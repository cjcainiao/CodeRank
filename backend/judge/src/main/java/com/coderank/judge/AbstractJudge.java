package com.coderank.judge;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.coderank.client.JudgeClient;
import com.coderank.client.MinioFileClient;
import com.coderank.entity.Question;
import com.coderank.entity.QuestionAnswerFile;
import com.coderank.entity.SubmissionTask;
import com.coderank.enums.ErrorCode;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.QuestionAnswerFileMapper;
import com.coderank.mapper.QuestionMapper;
import com.coderank.mapper.SubmissionTaskMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 评测流程抽象实现。
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractJudge implements Judge {

    private static final byte TASK_STATUS_RUNNING = 1;
    private static final byte TASK_STATUS_COMPLETED = 2;
    private static final byte TASK_STATUS_FAILED = 3;

    private static final String OUTPUT_SCRIPT_FILE_NAME = "output_script.py";
    private static final String PYTHON_PATH = "/usr/bin/python3";
    private static final int SCRIPT_OUTPUT_LIMIT = 1024 * 1024;

    protected final SubmissionTaskMapper submissionTaskMapper;
    protected final QuestionMapper questionMapper;
    protected final QuestionAnswerFileMapper questionAnswerFileMapper;
    protected final JudgeClient judgeClient;
    protected final MinioFileClient minioFileClient;


    // 查询任务
    public final void query(JudgeContext context) {
        if (context == null || context.getTaskId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评测任务id不能为空");
        }

        SubmissionTask task = submissionTaskMapper.selectById(context.getTaskId());
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评测任务不存在");
        }

        context.setTask(task);
        boolean testTask = Byte.valueOf((byte) 0).equals(task.getTaskType());

        if (!testTask) {
            if (task.getQuestionId() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "正式评测任务缺少题目id");
            }

            Question question = questionMapper.selectById(task.getQuestionId());
            if (question == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "题目不存在");
            }
            context.setQuestion(question);

            List<QuestionAnswerFile> answerFiles = questionAnswerFileMapper.selectList(
                    Wrappers.<QuestionAnswerFile>lambdaQuery()
                            .eq(QuestionAnswerFile::getQuestionId, task.getQuestionId())
                            .eq(QuestionAnswerFile::getStatus, (byte) 1)
                            .orderByAsc(QuestionAnswerFile::getCaseNo)
            );
            if (answerFiles.isEmpty()) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "题目答案文件不存在");
            }

            List<JudgeContext.JudgeData> judgeDataList = answerFiles.stream()
                    .map(answerFile -> {
                        JudgeContext.JudgeData judgeData = new JudgeContext.JudgeData();
                        judgeData.setBucketName(answerFile.getBucketName());
                        judgeData.setInputFilePath(answerFile.getInputFilePath());
                        judgeData.setOutputFilePath(answerFile.getOutputFilePath());
                        return judgeData;
                    })
                    .toList();
            context.setJudgeDataList(judgeDataList);
        }
    }

    // 校验代码参数
    public final void check(JudgeContext context) {
        String sourceCode = context.getTask().getSourceCode();
        if (sourceCode == null || sourceCode.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "提交代码不能为空");
        }

        int sourceCodeBytes = sourceCode.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        if (sourceCodeBytes > 65_535) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "提交代码长度不能超过65535字节");
        }
    }

    // 组装代码
    public void assembly(JudgeContext context) {
        String sourceCode = context.getTask().getSourceCode();
        String codeTemplate = context.getTask().getCodeTemplate();
        if (codeTemplate == null || codeTemplate.isBlank()) {
            context.setAssembledCode(sourceCode);
            return;
        }

        String placeholder = "{code_template}";
        if (!codeTemplate.contains(placeholder)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "代码模板缺少{code_template}占位符");
        }

        context.setAssembledCode(codeTemplate.replace(placeholder, sourceCode));
    }

    // 运行输出结果脚本
    public void runOutputScript(JudgeContext context) {
        try {
            if (!Byte.valueOf((byte) 1).equals(context.getQuestion().getOutputScript())) {
                return;
            }

            String scriptContent = context.getQuestion().getOutputScriptContent();
            if (scriptContent == null || scriptContent.isBlank()) {
                updateTaskStatus(context, TASK_STATUS_FAILED, "题目输出结果脚本为空");
                return;
            }

            // 获取刚刚执行完成的测试点输出。
            List<JsonNode> runResultList = context.getRunResultList();
            if (runResultList.isEmpty()) {
                updateTaskStatus(context, TASK_STATUS_FAILED, "测试点运行结果为空");
                return;
            }
            JsonNode lastRunResult = runResultList.get(runResultList.size() - 1);
            String output = lastRunResult.path("files").path("stdout").asText("");

            Map<String, Object> command = new LinkedHashMap<>();
            command.put("args", List.of(PYTHON_PATH, OUTPUT_SCRIPT_FILE_NAME));
            command.put("env", List.of("PATH=/usr/bin:/bin"));
            command.put("files", List.of(
                    Map.of("content", output),
                    Map.of("name", "stdout", "max", SCRIPT_OUTPUT_LIMIT),
                    Map.of("name", "stderr", "max", SCRIPT_OUTPUT_LIMIT)
            ));
            command.put("cpuLimit", 1_000_000_000L);
            command.put("clockLimit", 2_000_000_000L);
            command.put("memoryLimit", 128L * 1024 * 1024);
            command.put("procLimit", 10);
            command.put("copyIn", Map.of(
                    OUTPUT_SCRIPT_FILE_NAME,
                    Map.of("content", scriptContent)
            ));

            JsonNode response = judgeClient.run(Map.of("cmd", List.of(command)));
            if (!response.isArray() || response.isEmpty()) {
                updateTaskStatus(context, TASK_STATUS_FAILED, "输出结果脚本未返回结果");
                return;
            }

            JsonNode scriptResult = response.get(0);
            String status = scriptResult.path("status").asText();
            String error = scriptResult.path("files").path("stderr").asText("");
            if (!"Accepted".equals(status)) {
                updateTaskStatus(
                        context,
                        TASK_STATUS_FAILED,
                        "输出结果脚本执行失败：" + (error.isBlank() ? status : error)
                );
                return;
            }

            JsonNode files = lastRunResult.path("files");
            if (!(files instanceof ObjectNode filesNode)) {
                updateTaskStatus(context, TASK_STATUS_FAILED, "测试点输出结果格式错误");
                return;
            }
            // 使用脚本处理后的输出替换当前测试点输出。
            filesNode.put("stdout", scriptResult.path("files").path("stdout").asText(""));
        } catch (RuntimeException exception) {
            String errorMessage = exception.getMessage() == null
                    ? "输出结果脚本执行失败"
                    : exception.getMessage();
            updateTaskStatus(context, TASK_STATUS_FAILED, errorMessage);
            log.error("运行输出结果脚本失败，taskId={}", context.getTaskId(), exception);
        }
    }

    // 结果评测
    public void judge(JudgeContext context) {
        try {
            List<JsonNode> runResultList = context.getRunResultList();
            List<JudgeContext.JudgeData> judgeDataList = context.getJudgeDataList();
            if (runResultList.size() != judgeDataList.size()) {
                updateTaskStatus(context, TASK_STATUS_FAILED, "测试点运行结果数量不一致");
                return;
            }

            byte judgeResult = 1;
            String outputData = null;
            String errorMessage = null;
            int maxTimeUsed = 0;
            long maxMemoryUsed = 0L;

            for (int i = 0; i < runResultList.size(); i++) {
                JsonNode runResult = runResultList.get(i);
                if (!(runResult instanceof ObjectNode resultNode)) {
                    updateTaskStatus(context, TASK_STATUS_FAILED, "第 " + (i + 1) + " 个测试点结果格式错误");
                    return;
                }

                String runStatus = runResult.path("status").asText();
                String actualOutput = runResult.path("files").path("stdout").asText("");
                String runError = runResult.path("files").path("stderr").asText("");

                int timeUsed = (int) Math.ceil(runResult.path("time").asLong() / 1_000_000D);
                long memoryUsed = (long) Math.ceil(runResult.path("memory").asLong() / 1024D);
                maxTimeUsed = Math.max(maxTimeUsed, timeUsed);
                maxMemoryUsed = Math.max(maxMemoryUsed, memoryUsed);

                byte caseJudgeResult = 1;
                String caseErrorMessage = null;

                if (!"Accepted".equals(runStatus)) {
                    caseJudgeResult = mapJudgeResult(runStatus);
                    caseErrorMessage = runError.isBlank()
                            ? (runStatus.isBlank() ? "Runtime Error" : runStatus)
                            : runError;
                } else {
                    JudgeContext.JudgeData judgeData = judgeDataList.get(i);
                    String expectedOutput = StringUtils.hasText(judgeData.getBucketName())
                            ? minioFileClient.getFileContent(
                                    judgeData.getBucketName(),
                                    judgeData.getOutputFilePath()
                            )
                            : minioFileClient.getFileContent(judgeData.getOutputFilePath());

                    String normalizedActual = actualOutput
                            .replace("\r\n", "\n")
                            .replace('\r', '\n')
                            .stripTrailing();
                    String normalizedExpected = expectedOutput
                            .replace("\r\n", "\n")
                            .replace('\r', '\n')
                            .stripTrailing();
                    if (!normalizedExpected.equals(normalizedActual)) {
                        caseJudgeResult = 2;
                        caseErrorMessage = "第 " + (i + 1) + " 个测试点答案错误";
                    }
                }

                // 补充当前测试点的业务判题结果。
                resultNode.put("judgeResult", caseJudgeResult);

                // 整体结果取第一个未通过测试点的结果。
                if (judgeResult == 1 && caseJudgeResult != 1) {
                    judgeResult = caseJudgeResult;
                    outputData = actualOutput;
                    errorMessage = caseErrorMessage;
                }
            }

            SubmissionTask task = context.getTask();
            task.setStatus(TASK_STATUS_COMPLETED);
            task.setJudgeResult(judgeResult);
            task.setOutputData(outputData);
            task.setErrorMessage(errorMessage);
            task.setTimeUsed(maxTimeUsed);
            task.setMemoryUsed(maxMemoryUsed);
            task.setJudgeMessageList(runResultList.toString());
            submissionTaskMapper.updateById(task);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage() == null
                    ? "评测结果处理失败"
                    : exception.getMessage();
            updateTaskStatus(context, TASK_STATUS_FAILED, errorMessage);
            log.error("评测结果处理失败，taskId={}", context.getTaskId(), exception);
        }
    }

    // 清理任务产生的临时缓存文件
    public void cleanup(JudgeContext context) {
        if (context == null || context.getExecutableFileId() == null) {
            return;
        }

        String fileId = context.getExecutableFileId();
        try {
            judgeClient.deleteFile(fileId);
        } catch (RuntimeException exception) {
            log.warn("清理 go-judge 临时文件失败，fileId={}", fileId, exception);
        } finally {
            context.setExecutableFileId(null);
        }
    }

    // 编译代码
    public abstract void compile(JudgeContext context);

    // 运行任务代码
    public abstract void run(JudgeContext context);

    // 运行测试代码
    public abstract void runTest(JudgeContext context);


    // 运行评测任务流程
    public void executeTask(Long submissionId) {
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setTaskId(submissionId);
        try {
            // 1、查询任务
            query(judgeContext);
            updateTaskStatus(judgeContext, TASK_STATUS_RUNNING, null);

            // 2、检测危险命令
            check(judgeContext);

            // 3、组装代码
            assembly(judgeContext);

            // 4、编译代码
            compile(judgeContext);

            for (JudgeContext.JudgeData judgeData : judgeContext.getJudgeDataList()) {
                judgeContext.setCurrentJudgeData(judgeData);

                // 5、运行当前测试点代码
                run(judgeContext);

                // 6、运行输出结果脚本
                runOutputScript(judgeContext);
                if (Byte.valueOf(TASK_STATUS_FAILED).equals(judgeContext.getTask().getStatus())) {
                    return;
                }
            }

            // 7、获取结果并判题
            judge(judgeContext);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage() == null
                    ? "评测任务执行失败"
                    : exception.getMessage();
            updateTaskStatus(judgeContext, TASK_STATUS_FAILED, errorMessage);
            log.error("评测任务执行失败，taskId={}", submissionId, exception);
        } finally {
            // 8、清理任务资源
            cleanup(judgeContext);
        }
    }

    // 运行测试任务流程
    public void executeTestTask(Long submissionId) {
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setTaskId(submissionId);
        try {
            // 1、查询任务
            query(judgeContext);
            if (!Byte.valueOf((byte) 0).equals(judgeContext.getTask().getTaskType())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "当前任务不是测试任务");
            }
            // 标记任务正在运行。
            updateTaskStatus(judgeContext, TASK_STATUS_RUNNING, null);

            // 2、检测危险命令
            check(judgeContext);
            // 3、组装代码
            assembly(judgeContext);
            // 4、编译代码
            compile(judgeContext);
            // 5、运行代码
            runTest(judgeContext);
            // 6、获取输出结果并保存到数据库
            saveTestResult(judgeContext);
        } catch (RuntimeException exception) {
            // 流程异常时记录失败状态和错误原因。
            updateTaskStatus(judgeContext, TASK_STATUS_FAILED, exception.getMessage());
            throw exception;
        } finally {
            // 无论运行成功还是失败，都清理任务级临时文件。
            cleanup(judgeContext);
        }
    }

    /** 保存测试运行结果。 */
    private void saveTestResult(JudgeContext context) {
        List<JsonNode> runResultList = context.getRunResultList();
        if (runResultList.isEmpty()) {
            throw new BusinessException(ErrorCode.REMOTE_ERROR, "评测服务返回结果为空");
        }

        // 测试运行读取最后一次执行结果。
        JsonNode result = runResultList.get(runResultList.size() - 1);
        String runStatus = result.path("status").asText();
        String output = result.path("files").path("stdout").asText("");
        String error = result.path("files").path("stderr").asText("");
        if (!"Accepted".equals(runStatus) && error.isBlank()) {
            error = runStatus;
        }

        // go-judge 的耗时为纳秒、内存为字节，转换成数据库使用的单位。
        int timeUsed = (int) Math.ceil(result.path("time").asLong() / 1_000_000D);
        long memoryUsed = (long) Math.ceil(result.path("memory").asLong() / 1024D);
        byte judgeResult = mapJudgeResult(runStatus);

        // 保存任务状态、输出和资源使用情况。
        SubmissionTask task = context.getTask();
        task.setStatus(TASK_STATUS_COMPLETED);
        task.setJudgeResult(judgeResult);
        task.setOutputData(output);
        task.setErrorMessage(error.isBlank() ? null : error);
        task.setTimeUsed(timeUsed);
        task.setMemoryUsed(memoryUsed);
        task.setJudgeMessageList(runResultList.toString());
        submissionTaskMapper.updateById(task);
    }

    /** 更新任务状态。 */
    private void updateTaskStatus(JudgeContext context, byte status, String errorMessage) {
        if (context.getTask() != null) {
            context.getTask().setStatus(status);
            context.getTask().setErrorMessage(errorMessage);
        }

        SubmissionTask task = SubmissionTask.builder()
                .submissionId(context.getTaskId())
                .status(status)
                .errorMessage(errorMessage)
                .build();
        submissionTaskMapper.updateById(task);
    }

    /** 转换 go-judge 运行状态。 */
    private byte mapJudgeResult(String status) {
        return switch (status) {
            case "Accepted" -> 1;
            case "Time Limit Exceeded" -> 3;
            case "Memory Limit Exceeded" -> 4;
            case "Compile Error" -> 6;
            default -> 5;
        };
    }
}
