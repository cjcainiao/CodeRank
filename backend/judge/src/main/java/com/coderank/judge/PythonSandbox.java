package com.coderank.judge;

import com.coderank.client.JudgeClient;
import com.coderank.client.MinioFileClient;
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
 * Python 代码沙箱。
 */
@Component("Python")
public class PythonSandbox extends AbstractJudge {

    /** 语言类型。 */
    private static final String LANGUAGE_TYPE = "python";

    /** go-judge 中的 Python 解释器路径。 */
    private static final String COMPILER_PATH = "/usr/bin/python3";

    /** Python 源代码文件名。 */
    private static final String SOURCE_FILE_NAME = "main.py";

    /** CPU 时间限制（ns）。 */
    private static final long CPU_LIMIT = 1_000_000_000L;

    /** 实际运行时间限制（ns）。 */
    private static final long CLOCK_LIMIT = 2_000_000_000L;

    /** 内存限制（字节）。 */
    private static final long MEMORY_LIMIT = 128L * 1024 * 1024;

    /** 最大进程数量。 */
    private static final int PROCESS_LIMIT = 10;

    /** 输出大小限制（字节）。 */
    private static final int OUTPUT_LIMIT = 10_240;

    public PythonSandbox(SubmissionTaskMapper submissionTaskMapper,
                         QuestionMapper questionMapper,
                         QuestionAnswerFileMapper questionAnswerFileMapper,
                         JudgeClient judgeClient,
                         MinioFileClient minioFileClient) {
        super(submissionTaskMapper, questionMapper, questionAnswerFileMapper, judgeClient, minioFileClient);
    }

    @Override
    public void compile(JudgeContext context) {

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
        command.put("args", List.of(COMPILER_PATH, SOURCE_FILE_NAME));
        command.put("env", List.of("PATH=/usr/bin:/bin"));
        command.put("files", List.of(
                Map.of("content", inputData),
                Map.of("name", "stdout", "max", OUTPUT_LIMIT),
                Map.of("name", "stderr", "max", OUTPUT_LIMIT)
        ));
        command.put("cpuLimit", CPU_LIMIT);
        command.put("clockLimit", CLOCK_LIMIT);
        command.put("memoryLimit", MEMORY_LIMIT);
        command.put("procLimit", PROCESS_LIMIT);
        command.put("copyIn", Map.of(
                SOURCE_FILE_NAME,
                Map.of("content", context.getAssembledCode())
        ));

        JsonNode result = judgeClient.run(Map.of("cmd", List.of(command)));
        result.forEach(context.getRunResultList()::add);

    }

    @Override
    public void runTest(JudgeContext context) {
        String inputData = context.getTask().getInputData();

        Map<String, Object> command = new LinkedHashMap<>();
        command.put("args", List.of(COMPILER_PATH, SOURCE_FILE_NAME));
        command.put("env", List.of("PATH=/usr/bin:/bin"));
        command.put("files", List.of(
                Map.of("content", inputData == null ? "" : inputData),
                Map.of("name", "stdout", "max", OUTPUT_LIMIT),
                Map.of("name", "stderr", "max", OUTPUT_LIMIT)
        ));
        command.put("cpuLimit", CPU_LIMIT);
        command.put("clockLimit", CLOCK_LIMIT);
        command.put("memoryLimit", MEMORY_LIMIT);
        command.put("procLimit", PROCESS_LIMIT);
        command.put("copyIn", Map.of(
                SOURCE_FILE_NAME,
                Map.of("content", context.getAssembledCode())
        ));

        Map<String, Object> request = Map.of("cmd", List.of(command));

        JsonNode result = judgeClient.run(request);
        result.forEach(context.getRunResultList()::add);

    }

}
