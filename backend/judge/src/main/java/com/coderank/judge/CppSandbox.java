package com.coderank.judge;

import com.coderank.client.JudgeClient;
import com.coderank.client.MinioFileClient;
import com.coderank.enums.ErrorCode;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.QuestionAnswerFileMapper;
import com.coderank.mapper.QuestionMapper;
import com.coderank.mapper.SubmissionTaskMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * C++ 代码沙箱。
 */
@Component
public class CppSandbox extends AbstractJudge {

    /** 语言类型。 */
    private static final String LANGUAGE_TYPE = "cpp";

    /** go-judge 中的 C++ 编译器路径。 */
    private static final String COMPILER_PATH = "/usr/bin/g++";

    /** C++ 源代码文件名。 */
    private static final String SOURCE_FILE_NAME = "main.cpp";

    /** 编译后的可执行文件名。 */
    private static final String EXECUTABLE_FILE_NAME = "main";

    /** 编译 CPU 时间限制（ns）。 */
    private static final long COMPILE_CPU_LIMIT = 10_000_000_000L;

    /** 编译实际运行时间限制（ns）。 */
    private static final long COMPILE_CLOCK_LIMIT = 15_000_000_000L;

    /** 编译内存限制（字节）。 */
    private static final long COMPILE_MEMORY_LIMIT = 512L * 1024 * 1024;

    /** 编译最大进程数量。 */
    private static final int COMPILE_PROCESS_LIMIT = 50;

    /** 编译输出大小限制（字节）。 */
    private static final int OUTPUT_LIMIT = 1024 * 1024;

    /** 运行 CPU 时间限制（ns）。 */
    private static final long RUN_CPU_LIMIT = 1_000_000_000L;

    /** 运行实际时间限制（ns）。 */
    private static final long RUN_CLOCK_LIMIT = 2_000_000_000L;

    /** 运行内存限制（字节）。 */
    private static final long RUN_MEMORY_LIMIT = 128L * 1024 * 1024;

    /** 运行最大进程数量。 */
    private static final int RUN_PROCESS_LIMIT = 10;

    public CppSandbox(SubmissionTaskMapper submissionTaskMapper,
                      QuestionMapper questionMapper,
                      QuestionAnswerFileMapper questionAnswerFileMapper,
                      JudgeClient judgeClient,
                      MinioFileClient minioFileClient) {
        super(submissionTaskMapper, questionMapper, questionAnswerFileMapper, judgeClient, minioFileClient);
    }

    @Override
    public void compile(JudgeContext context) {
        Map<String, Object> command = new LinkedHashMap<>();
        command.put("args", List.of(
                COMPILER_PATH,
                SOURCE_FILE_NAME,
                "-std=c++17",
                "-o",
                EXECUTABLE_FILE_NAME
        ));
        command.put("env", List.of("PATH=/usr/bin:/bin"));
        command.put("files", List.of(
                Map.of("content", ""),
                Map.of("name", "stdout", "max", OUTPUT_LIMIT),
                Map.of("name", "stderr", "max", OUTPUT_LIMIT)
        ));
        command.put("cpuLimit", COMPILE_CPU_LIMIT);
        command.put("clockLimit", COMPILE_CLOCK_LIMIT);
        command.put("memoryLimit", COMPILE_MEMORY_LIMIT);
        command.put("procLimit", COMPILE_PROCESS_LIMIT);
        command.put("copyIn", Map.of(
                SOURCE_FILE_NAME,
                Map.of("content", context.getAssembledCode())
        ));
        // 编译成功后将可执行文件保存在 go-judge 文件缓存中。
        command.put("copyOutCached", List.of(EXECUTABLE_FILE_NAME));

        Map<String, Object> request = Map.of("cmd", List.of(command));

        JsonNode compileResult = judgeClient.run(request);
        if (!compileResult.isArray() || compileResult.isEmpty()) {
            throw new BusinessException(ErrorCode.REMOTE_ERROR, "编译服务返回结果为空");
        }

        JsonNode result = compileResult.get(0);
        String status = result.path("status").asText();
        String error = result.path("files").path("stderr").asText("");
        if (!"Accepted".equals(status)) {
            String errorMessage = error.isBlank() ? status : error;
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "C++ 代码编译失败：" + errorMessage);
        }

        String fileId = result.path("fileIds").path(EXECUTABLE_FILE_NAME).asText("");
        if (fileId.isBlank()) {
            throw new BusinessException(ErrorCode.REMOTE_ERROR, "编译成功但未返回可执行文件id");
        }
        context.setExecutableFileId(fileId);

    }

    @Override
    public void run(JudgeContext context) {
        JudgeContext.JudgeData judgeData = context.getCurrentJudgeData();
        String inputData = StringUtils.hasText(judgeData.getBucketName())
                ? minioFileClient.getFileContent(
                        judgeData.getBucketName(),
                        judgeData.getInputFilePath()
                )
                : minioFileClient.getFileContent(judgeData.getInputFilePath());

        Map<String, Object> command = new LinkedHashMap<>();
        command.put("args", List.of(EXECUTABLE_FILE_NAME));
        command.put("env", List.of("PATH=/usr/bin:/bin"));
        command.put("files", List.of(
                Map.of("content", inputData),
                Map.of("name", "stdout", "max", OUTPUT_LIMIT),
                Map.of("name", "stderr", "max", OUTPUT_LIMIT)
        ));
        command.put("cpuLimit", RUN_CPU_LIMIT);
        command.put("clockLimit", RUN_CLOCK_LIMIT);
        command.put("memoryLimit", RUN_MEMORY_LIMIT);
        command.put("procLimit", RUN_PROCESS_LIMIT);
        command.put("copyIn", Map.of(
                EXECUTABLE_FILE_NAME,
                Map.of("fileId", context.getExecutableFileId())
        ));

        JsonNode result = judgeClient.run(Map.of("cmd", List.of(command)));
        result.forEach(context.getRunResultList()::add);

    }

    @Override
    public void runTest(JudgeContext context) {
        String inputData = context.getTask().getInputData();

        Map<String, Object> command = new LinkedHashMap<>();
        command.put("args", List.of(EXECUTABLE_FILE_NAME));
        command.put("env", List.of("PATH=/usr/bin:/bin"));
        command.put("files", List.of(
                Map.of("content", inputData == null ? "" : inputData),
                Map.of("name", "stdout", "max", OUTPUT_LIMIT),
                Map.of("name", "stderr", "max", OUTPUT_LIMIT)
        ));
        command.put("cpuLimit", RUN_CPU_LIMIT);
        command.put("clockLimit", RUN_CLOCK_LIMIT);
        command.put("memoryLimit", RUN_MEMORY_LIMIT);
        command.put("procLimit", RUN_PROCESS_LIMIT);
        // 使用编译阶段缓存在 go-judge 中的可执行文件。
        command.put("copyIn", Map.of(
                EXECUTABLE_FILE_NAME,
                Map.of("fileId", context.getExecutableFileId())
        ));

        Map<String, Object> request = Map.of("cmd", List.of(command));

        JsonNode runResult = judgeClient.run(request);
        runResult.forEach(context.getRunResultList()::add);

    }
}
