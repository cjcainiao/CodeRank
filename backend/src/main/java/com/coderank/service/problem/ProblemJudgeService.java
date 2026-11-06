package com.coderank.service.problem;

import com.coderank.common.enums.ResultCodeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.coderank.common.enums.LanguageEnum;
import com.coderank.entity.dto.problem.SubmitCodeDTO;
import com.coderank.entity.pojo.JudgeMachine;
import com.coderank.entity.pojo.Question;
import com.coderank.entity.pojo.QuestionCodeTemplate;
import com.coderank.entity.pojo.QuestionTestData;
import com.coderank.entity.pojo.Submission;
import com.coderank.entity.vo.judge.TestSubmitResultVO;
import com.coderank.entity.vo.problem.JudgeCaseVO;
import com.coderank.entity.vo.problem.JudgeResultVO;
import com.coderank.entity.vo.problem.SubmissionStatusVO;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.backend.judge.JudgeMachineMapper;
import com.coderank.mapper.backend.question.QuestionMapper;
import com.coderank.mapper.backend.question.QuestionCodeTemplateMapper;
import com.coderank.mapper.backend.question.QuestionTestDataMapper;
import com.coderank.mapper.home.SubmissionMapper;
import com.coderank.properties.FileUploadProperties;
import com.coderank.properties.JudgeProperties;
import com.coderank.service.judge.CodeAssembler;
import com.coderank.service.judge.ExecOutcome;
import com.coderank.service.judge.JudgeExecutor;
import com.coderank.service.judge.JudgeLimits;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 做题页提交评测业务，同步执行且不落库。
 */
@Slf4j
@Service
public class ProblemJudgeService {

    /** 机器状态：启用 */
    private static final int STATUS_ENABLED = 1;

    /** 题目状态：已发布 */
    private static final int QUESTION_PUBLISHED = 1;

    /** 判题状态：通过 */
    private static final String STATUS_ACCEPTED = "Accepted";

    /** 判题状态：编译错误 */
    private static final String STATUS_COMPILE_ERROR = "Compile Error";

    /** 判题状态：答案错误 */
    private static final String STATUS_WRONG_ANSWER = "Wrong Answer";

    /** 判题状态：运行错误 */
    private static final String STATUS_RUNTIME_ERROR = "Runtime Error";

    /** 判题状态：超时 */
    private static final String STATUS_TIME_LIMIT = "Time Limit Exceeded";

    /** 判题状态：内存超限 */
    private static final String STATUS_MEMORY_LIMIT = "Memory Limit Exceeded";

    /** 判题状态：系统错误 */
    private static final String STATUS_SYSTEM_ERROR = "System Error";

    /** 判题状态：等待 */
    private static final String STATUS_PENDING = "Pending";

    /** 判题状态：评测中 */
    private static final String STATUS_RUNNING = "Running";

    /** 判题模式：核心代码，需要用桩程序拼接完整程序 */
    private static final int JUDGE_MODE_CORE = 2;

    /** 测试点可见性：展示，只有展示型测试点参与评测 */
    private static final int VISIBLE_SHOWN = 1;

    /** 提交状态：等待 */
    private static final int SUBMISSION_STATUS_PENDING = 0;

    /** 提交状态：评测中 */
    private static final int SUBMISSION_STATUS_RUNNING = 8;

    /** 提交状态：通过 */
    private static final int STATUS_ACCEPTED_VALUE = 1;

    /** 提交状态：答案错误 */
    private static final int STATUS_WRONG_ANSWER_VALUE = 2;

    /** 提交状态：超时 */
    private static final int STATUS_TIME_LIMIT_VALUE = 3;

    /** 提交状态：内存超限 */
    private static final int STATUS_MEMORY_LIMIT_VALUE = 4;

    /** 提交状态：运行错误 */
    private static final int STATUS_RUNTIME_ERROR_VALUE = 5;

    /** 提交状态：编译错误 */
    private static final int STATUS_COMPILE_ERROR_VALUE = 6;

    /** 提交状态：系统错误 */
    private static final int STATUS_SYSTEM_ERROR_VALUE = 7;

    /** 输入文件扩展名 */
    private static final String EXTENSION_IN = "in";

    /** 输出文件扩展名 */
    private static final String EXTENSION_OUT = "out";

    private final QuestionMapper questionMapper;

    private final QuestionTestDataMapper questionTestDataMapper;

    private final JudgeMachineMapper judgeMachineMapper;

    private final JudgeExecutor judgeExecutor;

    private final FileUploadProperties fileUploadProperties;

    private final SubmissionMapper submissionMapper;

    private final QuestionCodeTemplateMapper codeTemplateMapper;

    private final CodeAssembler codeAssembler;

    private final JudgeProperties judgeProperties;

    private final ThreadPoolTaskExecutor judgeTaskExecutor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProblemJudgeService(QuestionMapper questionMapper,
                               QuestionTestDataMapper questionTestDataMapper,
                               JudgeMachineMapper judgeMachineMapper,
                               JudgeExecutor judgeExecutor,
                               FileUploadProperties fileUploadProperties,
                               SubmissionMapper submissionMapper,
                               QuestionCodeTemplateMapper codeTemplateMapper,
                               CodeAssembler codeAssembler,
                               JudgeProperties judgeProperties,
                               @Qualifier("judgeTaskExecutor") ThreadPoolTaskExecutor judgeTaskExecutor) {
        this.questionMapper = questionMapper;
        this.questionTestDataMapper = questionTestDataMapper;
        this.judgeMachineMapper = judgeMachineMapper;
        this.judgeExecutor = judgeExecutor;
        this.fileUploadProperties = fileUploadProperties;
        this.submissionMapper = submissionMapper;
        this.codeTemplateMapper = codeTemplateMapper;
        this.codeAssembler = codeAssembler;
        this.judgeProperties = judgeProperties;
        this.judgeTaskExecutor = judgeTaskExecutor;
    }

    // 提交代码，落一条等待中的任务后异步评测，立即返回任务 id
    public Long submit(SubmitCodeDTO submitDTO) {
        LanguageEnum language = LanguageEnum.fromCode(submitDTO.getLanguage())
                .orElseThrow(() -> new BusinessException(ResultCodeEnum.BAD_REQUEST, "不支持的语言"));

        Question question = questionMapper.selectById(submitDTO.getQuestionId());
        if (question == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "题目不存在");
        }

        // 提交时先校验测试数据，避免用户等一轮轮询才看到错误
        List<JudgeCaseData> caseDataList = loadCaseData(question);
        if (caseDataList.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "该题目还没有配置测试数据");
        }

        Submission submission = Submission.builder()
                .questionId(question.getId())
                .language(language.getCode())
                .sourceCode(submitDTO.getCode())
                .status(SUBMISSION_STATUS_PENDING)
                .passedCount(0)
                .totalCount(caseDataList.size())
                .timeUsed(0)
                .memoryUsed(0L)
                .caseResults("[]")
                .build();
        submissionMapper.insert(submission);

        // 判题放到线程池执行，接口立即返回任务 id 供前端轮询
        Long submissionId = submission.getId();
        judgeTaskExecutor.execute(() -> runJudge(submission.getId(), question, language,
                submitDTO.getCode(), caseDataList));

        return submissionId;
    }

    // 异步判题：编译一次后跑完全部测试点，结果写回提交记录
    private void runJudge(Long submissionId, Question question, LanguageEnum language,
                          String sourceCode, List<JudgeCaseData> caseDataList) {
        Submission submission = Submission.builder()
                .id(submissionId)
                .status(SUBMISSION_STATUS_RUNNING)
                .build();
        submissionMapper.updateById(submission);

        List<String> inputs = caseDataList.stream().map(JudgeCaseData::input).toList();
        List<String> expectedOutputs = caseDataList.stream().map(JudgeCaseData::expected).toList();

        try {
            JudgeMachine machine = pickRandomMachine();
            JudgeLimits limits = JudgeLimits.ofQuestion(
                    question.getTimeLimit(), question.getMemoryLimit(), judgeProperties);
            // 核心代码模式先按桩程序拼成完整程序
            String code = assembleCode(question, language, sourceCode);

            ExecOutcome outcome = judgeExecutor.executeAll(machine, language, code, inputs, limits);
            JudgeResultVO result = buildResult(question.getId(), caseDataList, expectedOutputs, outcome);
            saveSubmission(submissionId, machine, result);
            updateQuestionCounters(question.getId(), result.getAccepted());
        } catch (RuntimeException e) {
            log.error("评测任务执行失败，submissionId={}", submissionId, e);
            markSubmissionFailed(submissionId, e.getMessage());
        }
    }

    // 查询评测任务状态，供前端轮询
    public SubmissionStatusVO getSubmissionStatus(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "提交记录不存在");
        }

        boolean finished = submission.getStatus() != null
                && submission.getStatus() != SUBMISSION_STATUS_PENDING
                && submission.getStatus() != SUBMISSION_STATUS_RUNNING;

        return SubmissionStatusVO.builder()
                .submissionId(submission.getId())
                .status(submission.getStatus())
                .finished(finished)
                .result(finished ? toStatusName(submission.getStatus()) : null)
                .accepted(finished && Integer.valueOf(STATUS_ACCEPTED_VALUE).equals(submission.getStatus()))
                .passedCount(submission.getPassedCount())
                .totalCount(submission.getTotalCount())
                .timeUsed(submission.getTimeUsed())
                .memoryUsed(submission.getMemoryUsed())
                .message(submission.getErrorMessage())
                .cases(finished ? readCaseResults(submission.getCaseResults()) : null)
                .build();
    }

    // 数据库状态值转判题状态名
    private String toStatusName(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case STATUS_ACCEPTED_VALUE -> STATUS_ACCEPTED;
            case STATUS_WRONG_ANSWER_VALUE -> STATUS_WRONG_ANSWER;
            case STATUS_TIME_LIMIT_VALUE -> STATUS_TIME_LIMIT;
            case STATUS_MEMORY_LIMIT_VALUE -> STATUS_MEMORY_LIMIT;
            case STATUS_RUNTIME_ERROR_VALUE -> STATUS_RUNTIME_ERROR;
            case STATUS_COMPILE_ERROR_VALUE -> STATUS_COMPILE_ERROR;
            case STATUS_SYSTEM_ERROR_VALUE -> STATUS_SYSTEM_ERROR;
            case SUBMISSION_STATUS_PENDING -> STATUS_PENDING;
            case SUBMISSION_STATUS_RUNNING -> STATUS_RUNNING;
            default -> null;
        };
    }

    // 读取 jsonb 里的测试点详情，解析失败时返回空列表
    private List<JudgeCaseVO> readCaseResults(String caseResults) {
        if (!StringUtils.hasText(caseResults)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(caseResults,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, JudgeCaseVO.class));
        } catch (JsonProcessingException e) {
            log.warn("测试点详情解析失败", e);
            return List.of();
        }
    }

    // 提交评测只读展示型测试点，隐藏测试点不发给评测机
    private List<JudgeCaseData> loadCaseData(Question question) {
        List<QuestionTestData> testDataList = questionTestDataMapper.selectList(
                new LambdaQueryWrapper<QuestionTestData>()
                        .eq(QuestionTestData::getQuestionId, question.getId())
                        .eq(QuestionTestData::getVisible, VISIBLE_SHOWN)
                        .orderByAsc(QuestionTestData::getCaseNo));

        List<JudgeCaseData> result = new ArrayList<>();
        for (QuestionTestData testData : testDataList) {
            String input = readTestFile(question.getId(), testData.getCaseNo(), EXTENSION_IN, true);
            String expected = readTestFile(question.getId(), testData.getCaseNo(), EXTENSION_OUT, false);
            result.add(new JudgeCaseData(testData.getCaseNo(), input, expected));
        }
        return result;
    }

    // 读取本地测试数据文件
    private String readTestFile(Long questionId, Integer caseNo, String extension, boolean required) {
        Path path = resolveDirectory(questionId).resolve(caseNo + "." + extension);
        if (!Files.exists(path)) {
            if (required) {
                throw new BusinessException(ResultCodeEnum.NOT_FOUND, "测试数据文件缺失：" + caseNo + "." + extension);
            }
            return "";
        }
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取测试数据失败，path={}", path, e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "测试数据读取失败");
        }
    }

    // 评测数据目录为「上传根目录/评测数据子目录/题目 id」
    private Path resolveDirectory(Long questionId) {
        FileUploadProperties.TestData config = fileUploadProperties.getTestData();
        return Paths.get(fileUploadProperties.getRoot(), config.getDir(), String.valueOf(questionId));
    }

    /**
     * 一个测试点的输入与期望输出，只有展示型测试点会进入评测。
     */
    private record JudgeCaseData(Integer caseNo, String input, String expected) {
    }

    // 汇总各测试点结果
    private JudgeResultVO buildResult(Long questionId, List<JudgeCaseData> caseDataList,
                                      List<String> expectedOutputs, ExecOutcome outcome) {
        // 编译失败时没有测试点结果，整体状态直接取编译阶段
        TestSubmitResultVO compileResult = outcome.compileResult();
        if (compileResult != null) {
            return JudgeResultVO.builder()
                    .questionId(questionId)
                    .totalCount(caseDataList.size())
                    .passedCount(0)
                    .accepted(false)
                    .timeUsed(0)
                    .memoryUsed(0L)
                    .status(STATUS_COMPILE_ERROR)
                    .message(StringUtils.hasText(compileResult.getStderr())
                            ? compileResult.getStderr() : compileResult.getStatus())
                    .cases(List.of())
                    .build();
        }

        List<JudgeCaseVO> cases = new ArrayList<>();
        List<TestSubmitResultVO> results = outcome.results();
        int passedCount = 0;
        int maxTime = 0;
        long maxMemory = 0L;
        String overallStatus = STATUS_ACCEPTED;
        String overallMessage = null;

        for (int i = 0; i < results.size(); i++) {
            TestSubmitResultVO result = results.get(i);
            JudgeCaseData caseData = caseDataList.get(i);

            int timeMs = result.getTime() == null ? 0 : (int) Math.ceil(result.getTime() / 1_000_000D);
            long memoryKb = result.getMemory() == null ? 0L : (long) Math.ceil(result.getMemory() / 1024D);
            maxTime = Math.max(maxTime, timeMs);
            maxMemory = Math.max(maxMemory, memoryKb);

            JudgeCaseVO caseVO = buildCaseVO(caseData, result, expectedOutputs.get(i), timeMs, memoryKb);
            if (Boolean.TRUE.equals(caseVO.getPassed())) {
                passedCount++;
            } else if (STATUS_ACCEPTED.equals(overallStatus)) {
                // 整体状态取第一个未通过测试点的结果
                overallStatus = caseVO.getStatus();
                overallMessage = caseVO.getMessage();
            }
            cases.add(caseVO);
        }

        return JudgeResultVO.builder()
                .questionId(questionId)
                .totalCount(caseDataList.size())
                .passedCount(passedCount)
                .accepted(passedCount == caseDataList.size())
                .timeUsed(maxTime)
                .memoryUsed(maxMemory)
                .status(overallStatus)
                .message(overallMessage)
                .cases(cases)
                .build();
    }

    // 单个测试点结果，参与评测的都是展示型测试点，可直接回显输出
    private JudgeCaseVO buildCaseVO(JudgeCaseData caseData, TestSubmitResultVO result,
                                    String expectedOutput, int timeMs, long memoryKb) {
        String status = result.getStatus();
        String stderr = result.getStderr();

        // 运行层面的错误，直接取评测机状态与 stderr
        if (!STATUS_ACCEPTED.equals(status)) {
            return JudgeCaseVO.builder()
                    .caseNo(caseData.caseNo())
                    .exitStatus(result.getExitStatus())
                    .time(timeMs)
                    .memory(memoryKb)
                    .passed(false)
                    .status(status)
                    .message(StringUtils.hasText(stderr) ? stderr : status)
                    .build();
        }

        String actual = result.getStdout() == null ? "" : result.getStdout();
        boolean matched = outputMatches(actual, expectedOutput);
        return JudgeCaseVO.builder()
                .caseNo(caseData.caseNo())
                .exitStatus(result.getExitStatus())
                .time(timeMs)
                .memory(memoryKb)
                .passed(matched)
                .status(matched ? STATUS_ACCEPTED : STATUS_WRONG_ANSWER)
                .message(matched ? null : "第 " + caseData.caseNo() + " 个测试点答案错误")
                .output(actual)
                .build();
    }

    // 逐行去除行尾空白与末尾空行后比较，与常见 OJ 的比对规则一致
    private String normalizeOutput(String text) {
        return text
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .lines()
                .map(String::stripTrailing)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("")
                .stripTrailing();
    }

    // 输出比对：两侧都是合法 JSON 时按结构比较，否则退回文本比较
    private boolean outputMatches(String actual, String expected) {
        String actualText = actual == null ? "" : actual;
        String expectedText = expected == null ? "" : expected;
        if (normalizeOutput(actualText).equals(normalizeOutput(expectedText))) {
            return true;
        }

        // 力扣题的期望输出是 JSON，空白差异不该判错
        JsonNode actualNode = tryParseJson(actualText);
        JsonNode expectedNode = tryParseJson(expectedText);
        return actualNode != null && expectedNode != null && actualNode.equals(expectedNode);
    }

    // 尝试解析 JSON，失败返回空
    private JsonNode tryParseJson(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        char first = trimmed.charAt(0);
        // 只对数组和对象做结构化比较，裸数字或字符串仍走文本比较
        if (first != '[' && first != '{' && !"true".equals(trimmed)
                && !"false".equals(trimmed) && !"null".equals(trimmed)) {
            return null;
        }
        try {
            return objectMapper.readTree(trimmed);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    // 在启用的机器中随机取一台
    private JudgeMachine pickRandomMachine() {
        List<JudgeMachine> machines = judgeMachineMapper.selectList(
                new LambdaQueryWrapper<JudgeMachine>()
                        .eq(JudgeMachine::getStatus, STATUS_ENABLED)
                        .isNotNull(JudgeMachine::getEndpoint));

        List<JudgeMachine> usable = machines.stream()
                .filter(machine -> StringUtils.hasText(machine.getEndpoint()))
                .toList();
        if (usable.isEmpty()) {
            throw new BusinessException("没有可用的评测机，请先在后台配置并启用");
        }

        return usable.get(ThreadLocalRandom.current().nextInt(usable.size()));
    }

    // 累加题目的提交次数与通过次数
    private void updateQuestionCounters(Long questionId, Boolean accepted) {
        LambdaUpdateWrapper<Question> wrapper = new LambdaUpdateWrapper<Question>()
                .eq(Question::getId, questionId)
                .setSql("submit_count = submit_count + 1");
        if (Boolean.TRUE.equals(accepted)) {
            wrapper.setSql("accepted_count = accepted_count + 1");
        }
        questionMapper.update(null, wrapper);
    }

    // 核心代码模式按桩程序拼接，标准输入输出模式原样使用
    private String assembleCode(Question question, LanguageEnum language, String sourceCode) {
        if (!Integer.valueOf(JUDGE_MODE_CORE).equals(question.getJudgeMode())) {
            return sourceCode;
        }

        QuestionCodeTemplate template = codeTemplateMapper.selectOne(
                new LambdaQueryWrapper<QuestionCodeTemplate>()
                        .eq(QuestionCodeTemplate::getQuestionId, question.getId())
                        .eq(QuestionCodeTemplate::getLanguage, toLanguageValue(language)));

        if (template == null || !StringUtils.hasText(template.getStubTemplate())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "该题缺少「" + language.getCode() + "」语言的桩程序，无法评测");
        }
        return codeAssembler.assemble(sourceCode, template.getStubTemplate());
    }

    // 语言枚举转题库模板的 language 值
    private int toLanguageValue(LanguageEnum language) {
        return switch (language) {
            case JAVA -> 1;
            case CPP -> 2;
            case PYTHON -> 3;
            case GO -> 4;
            case JAVASCRIPT -> 5;
        };
    }

    // 结果写入提交记录
    private void saveSubmission(Long submissionId, JudgeMachine machine, JudgeResultVO result) {
        Submission submission = Submission.builder()
                .id(submissionId)
                .status(toStatusValue(result.getStatus()))
                .passedCount(result.getPassedCount())
                .totalCount(result.getTotalCount())
                .timeUsed(result.getTimeUsed())
                .memoryUsed(result.getMemoryUsed())
                .errorMessage(result.getMessage())
                .caseResults(writeCaseResults(result.getCases()))
                .judgeMachineId(machine.getId())
                .build();
        submissionMapper.updateById(submission);
    }

    // 判题过程中的异常也要留痕，避免记录一直停在评测中
    private void markSubmissionFailed(Long submissionId, String message) {
        Submission submission = Submission.builder()
                .id(submissionId)
                .status(STATUS_SYSTEM_ERROR_VALUE)
                .errorMessage(message)
                .build();
        submissionMapper.updateById(submission);
    }

    // 测试点详情序列化为 jsonb
    private String writeCaseResults(List<JudgeCaseVO> cases) {
        try {
            return objectMapper.writeValueAsString(cases);
        } catch (JsonProcessingException e) {
            log.warn("测试点结果序列化失败，caseCount={}", cases.size(), e);
            return "[]";
        }
    }

    // 判题状态文本转数据库枚举值
    private int toStatusValue(String status) {
        return switch (status == null ? "" : status) {
            case STATUS_ACCEPTED -> STATUS_ACCEPTED_VALUE;
            case STATUS_WRONG_ANSWER -> STATUS_WRONG_ANSWER_VALUE;
            case "Time Limit Exceeded" -> STATUS_TIME_LIMIT_VALUE;
            case "Memory Limit Exceeded" -> STATUS_MEMORY_LIMIT_VALUE;
            case STATUS_COMPILE_ERROR -> STATUS_COMPILE_ERROR_VALUE;
            case STATUS_RUNTIME_ERROR, "Nonzero Exit Status" -> STATUS_RUNTIME_ERROR_VALUE;
            default -> STATUS_SYSTEM_ERROR_VALUE;
        };
    }
}

