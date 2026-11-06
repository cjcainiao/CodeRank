package com.coderank.service.judge;

import com.coderank.common.enums.LanguageEnum;
import com.coderank.entity.pojo.JudgeMachine;
import com.coderank.entity.vo.judge.TestSubmitResultVO;
import com.coderank.exception.BusinessException;
import com.coderank.properties.JudgeProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 评测机执行器，按 go-judge 协议完成一次编译与运行，不落库。
 */
@Slf4j
@Component
public class JudgeExecutor {

    /** 执行阶段：编译 */
    public static final String STAGE_COMPILE = "compile";

    /** 执行阶段：运行 */
    public static final String STAGE_RUN = "run";

    /** 判题状态：通过 */
    private static final String JUDGE_STATUS_ACCEPTED = "Accepted";

    /** 输出捕获上限，字节 */
    private static final int CAPTURE_MAX = 65536;

    /** 可执行文件权限，八进制 0755 */
    private static final int EXECUTABLE_MODE = 493;

    /** 输出流名称：标准输出 */
    private static final String FILE_STDOUT = "stdout";

    /** 输出流名称：标准错误 */
    private static final String FILE_STDERR = "stderr";

    private final CloseableHttpClient judgeHttpClient;

    private final JudgeProperties judgeProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JudgeExecutor(@Qualifier("judgeHttpClient") CloseableHttpClient judgeHttpClient,
                         JudgeProperties judgeProperties) {
        this.judgeHttpClient = judgeHttpClient;
        this.judgeProperties = judgeProperties;
    }

    // 在指定评测机上运行一次代码，需要编译的语言先编译
    public TestSubmitResultVO execute(JudgeMachine machine, LanguageEnum language, String code,
                                      String input, JudgeLimits limits) {
        ExecOutcome outcome = executeAll(machine, language, code, List.of(input == null ? "" : input), limits);
        return outcome.results().isEmpty() ? outcome.compileResult() : outcome.results().get(0);
    }

    // 编译一次后依次运行多个测试点，返回每个测试点的独立结果
    public ExecOutcome executeAll(JudgeMachine machine, LanguageEnum language, String code,
                                  List<String> inputs, JudgeLimits limits) {
        ExecOutcome.ExecOutcomeBuilder builder = ExecOutcome.builder();
        String artifactFileId = null;
        try {
            if (language.needsCompile()) {
                JsonNode compileResult = compile(machine, language, code);
                TestSubmitResultVO compileVO = toResultVO(machine, STAGE_COMPILE, compileResult);
                if (!isAccepted(compileVO)) {
                    // 编译失败时没有测试点结果，直接返回编译阶段错误
                    return builder.compileResult(compileVO).results(List.of()).build();
                }
                artifactFileId = artifactFileIdOf(compileResult, language.getArtifactName());
            }

            List<TestSubmitResultVO> results = new ArrayList<>();
            for (String input : inputs) {
                JsonNode runResult = run(machine, language, code, input, artifactFileId, limits);
                results.add(toResultVO(machine, STAGE_RUN, runResult));
            }
            return builder.results(results).build();
        } finally {
            if (artifactFileId != null) {
                deleteArtifact(machine, artifactFileId);
            }
        }
    }

    // 编译阶段：源码写入沙箱，产物用 copyOutCached 留在评测机
    private JsonNode compile(JudgeMachine machine, LanguageEnum language, String code) {
        ObjectNode cmd = newCommand(language.getCompileCommand());
        applyCaptureFiles(cmd, "");
        cmd.putObject("copyIn")
                .putObject(language.getSourceName())
                .put("content", code);
        cmd.putArray("copyOutCached").add(language.getArtifactName());
        applyCompileLimits(cmd);

        return postRun(machine, wrapRequest(cmd));
    }

    // 运行阶段：编译产物按 fileId 挂回沙箱，标准输入由调用方提供
    private JsonNode run(JudgeMachine machine, LanguageEnum language, String code, String input,
                         String artifactFileId, JudgeLimits limits) {
        ObjectNode cmd = newCommand(language.getRunCommand());
        applyCaptureFiles(cmd, input == null ? "" : input);

        if (language.needsCompile()) {
            cmd.putObject("copyIn")
                    .putObject(language.getArtifactName())
                    .put("fileId", artifactFileId)
                    .put("mode", EXECUTABLE_MODE);
        } else {
            cmd.putObject("copyIn")
                    .putObject(language.getSourceName())
                    .put("content", code);
        }

        applyRunLimits(cmd, machine, limits);
        return postRun(machine, wrapRequest(cmd));
    }

    // 建一个 cmd 节点，固定写入启动命令与 PATH
    private ObjectNode newCommand(List<String> args) {
        ObjectNode cmd = objectMapper.createObjectNode();
        ArrayNode argNodes = cmd.putArray("args");
        args.forEach(argNodes::add);
        cmd.putArray("env").add("PATH=" + judgeProperties.getSandbox().getPath());
        return cmd;
    }

    // 文件槽位固定：0 标准输入，1 标准输出，2 标准错误
    private void applyCaptureFiles(ObjectNode cmd, String stdinContent) {
        ArrayNode files = cmd.putArray("files");
        // 空对象会被评测机判为非法文件，没有输入时也得给出空内容
        files.addObject().put("content", stdinContent == null ? "" : stdinContent);
        files.addObject().put("name", FILE_STDOUT).put("max", CAPTURE_MAX);
        files.addObject().put("name", FILE_STDERR).put("max", CAPTURE_MAX);
    }

    // 编译比运行更吃资源，限额单独取自配置
    private void applyCompileLimits(ObjectNode cmd) {
        JudgeProperties.Compile compile = judgeProperties.getCompile();
        cmd.put("cpuLimit", toNanos(compile.getCpuLimit()));
        cmd.put("clockLimit", toNanos(compile.getClockLimit()));
        cmd.put("memoryLimit", compile.getMemoryLimit());
        cmd.put("procLimit", compile.getProcLimit());
        cmd.put("outputLimit", judgeProperties.getRun().getOutputLimit());
    }

    // 运行限额按传入的限额下发，未传时回退到配置的兜底值
    private void applyRunLimits(ObjectNode cmd, JudgeMachine machine, JudgeLimits limits) {
        if (limits != null) {
            cmd.put("cpuLimit", limits.cpuLimit());
            cmd.put("clockLimit", limits.clockLimit());
            cmd.put("memoryLimit", limits.memoryLimit());
            cmd.put("procLimit", limits.procLimit());
            cmd.put("outputLimit", limits.outputLimit());
            return;
        }
        JudgeProperties.Run run = judgeProperties.getRun();
        cmd.put("cpuLimit", limitOr(machine.getCpuLimit(), toNanos(run.getCpuLimit())));
        cmd.put("clockLimit", limitOr(machine.getClockLimit(), toNanos(run.getClockLimit())));
        cmd.put("memoryLimit", limitOr(machine.getMemoryLimit(), run.getMemoryLimit()));
        cmd.put("procLimit", limitOr(
                machine.getProcLimit() == null ? null : machine.getProcLimit().longValue(), run.getProcLimit()));
        cmd.put("outputLimit", limitOr(machine.getOutputLimit(), run.getOutputLimit()));
    }

    // Duration 转纳秒，评测机要求的时间单位
    private long toNanos(Duration duration) {
        return duration == null ? 0L : duration.toNanos();
    }

    // 包一层 cmd 数组，go-judge 的 /run 请求体格式
    private String wrapRequest(ObjectNode cmd) {
        ObjectNode root = objectMapper.createObjectNode();
        root.putArray("cmd").add(cmd);
        return root.toString();
    }

    // 发起 /run 请求并返回首个执行结果
    private JsonNode postRun(JudgeMachine machine, String requestBody) {
        HttpPost request = new HttpPost(buildRunUrl(machine.getEndpoint()));
        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
        if (StringUtils.hasText(machine.getAuthToken())) {
            request.setHeader("Authorization", "Bearer " + machine.getAuthToken());
        }

        HttpResult response;
        try {
            // 回调内只读取响应，状态码判断放到外层，避免异常被客户端包装后丢失原始信息
            response = judgeHttpClient.execute(request, resp -> new HttpResult(
                    resp.getCode(),
                    EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.warn("代码运行请求失败，machineId={}，endpoint={}，原因={}",
                    machine.getId(), machine.getEndpoint(), describeError(e));
            throw new BusinessException("评测机请求失败：" + describeError(e));
        }

        if (response.status() != 200) {
            throw new BusinessException("评测机返回状态码 " + response.status());
        }

        try {
            JsonNode root = objectMapper.readTree(response.body());
            if (root.isArray()) {
                return root.isEmpty() ? objectMapper.createObjectNode() : root.get(0);
            }
            return root;
        } catch (Exception e) {
            log.warn("评测机响应解析失败，machineId={}，原因={}", machine.getId(), e.getMessage());
            throw new BusinessException("评测机响应格式异常");
        }
    }

    // 删除评测机上的编译产物，失败只记日志，不影响已经拿到的运行结果
    private void deleteArtifact(JudgeMachine machine, String fileId) {
        HttpDelete request = new HttpDelete(buildFileUrl(machine.getEndpoint(), fileId));
        if (StringUtils.hasText(machine.getAuthToken())) {
            request.setHeader("Authorization", "Bearer " + machine.getAuthToken());
        }
        try {
            judgeHttpClient.execute(request, response -> {
                if (response.getCode() != 200 && response.getCode() != 404) {
                    log.warn("编译产物清理失败，machineId={}，fileId={}，状态码={}",
                            machine.getId(), fileId, response.getCode());
                }
                return null;
            });
        } catch (Exception e) {
            log.warn("编译产物清理异常，machineId={}，fileId={}，原因={}",
                    machine.getId(), fileId, describeError(e));
        }
    }

    // 拼接运行接口地址，容忍地址末尾多写斜杠
    private String buildRunUrl(String endpoint) {
        String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return base + "/run";
    }

    // 拼接单文件接口地址，容忍地址末尾多写斜杠
    private String buildFileUrl(String endpoint, String fileId) {
        String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return base + "/file/" + fileId;
    }

    // 是否正常结束，状态为 Accepted 且无评测机层错误
    private boolean isAccepted(TestSubmitResultVO vo) {
        return JUDGE_STATUS_ACCEPTED.equals(vo.getStatus()) && vo.getError() == null;
    }

    // 从响应的 fileIds 中取编译产物 id
    private String artifactFileIdOf(JsonNode result, String artifactName) {
        JsonNode fileIds = result.get("fileIds");
        JsonNode fileId = fileIds == null ? null : fileIds.get(artifactName);
        if (fileId == null || fileId.isNull() || !StringUtils.hasText(fileId.asText())) {
            throw new BusinessException("评测机未返回编译产物，请确认机器支持 copyOutCached");
        }
        return fileId.asText();
    }

    // 结果节点转视图对象，输出内容从文件列表中按名称取出
    private TestSubmitResultVO toResultVO(JudgeMachine machine, String stage, JsonNode result) {
        JsonNode files = result.get("files");
        return TestSubmitResultVO.builder()
                .machineId(machine.getId())
                .machineName(machine.getName())
                .stage(stage)
                .status(textOrNull(result, "status"))
                .exitStatus(intOrNull(result, "exitStatus"))
                .time(longOrNull(result, "time"))
                .memory(longOrNull(result, "memory"))
                .error(textOrNull(result, "error"))
                .stdout(readFileContent(files, FILE_STDOUT))
                .stderr(readFileContent(files, FILE_STDERR))
                .build();
    }

    // 从输出文件中取指定名称的内容，兼容新版 map 与旧版数组两种形式
    private String readFileContent(JsonNode files, String name) {
        if (files == null || files.isNull()) {
            return null;
        }
        if (files.isObject()) {
            JsonNode value = files.get(name);
            return value == null || value.isNull() ? null : value.asText();
        }
        if (files.isArray()) {
            for (JsonNode file : files) {
                if (name.equals(textOrNull(file, "name"))) {
                    return textOrNull(file, "content");
                }
            }
        }
        return null;
    }

    // 限制值缺失时回退到兜底值
    private long limitOr(Long value, long fallback) {
        return value == null || value < 1 ? fallback : value;
    }

    // 读取文本字段，缺失时返回空
    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    // 读取整型字段，缺失时返回空
    private Integer intOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asInt();
    }

    // 读取长整型字段，缺失时返回空
    private Long longOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asLong();
    }

    // 把网络异常翻译成可直接展示的中文原因
    private String describeError(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        String type = cause.getClass().getSimpleName();
        if (type.contains("ConnectTimeout") || cause instanceof ConnectException) {
            return "连接超时，请确认评测机已启动且端口对外开放";
        }
        if (type.contains("SocketTimeout") || cause instanceof SocketTimeoutException) {
            return "请求超时，评测机响应过慢";
        }
        if (cause instanceof UnknownHostException) {
            return "无法解析服务地址";
        }
        return cause.getMessage() == null ? type : cause.getMessage();
    }

    /**
     * 状态码与响应体。
     */
    private record HttpResult(int status, String body) {
    }
}
