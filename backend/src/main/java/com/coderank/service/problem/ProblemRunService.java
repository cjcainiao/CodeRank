package com.coderank.service.problem;

import com.coderank.common.enums.ResultCodeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coderank.common.enums.LanguageEnum;
import com.coderank.entity.dto.problem.RunCodeDTO;
import com.coderank.entity.pojo.JudgeMachine;
import com.coderank.entity.pojo.Question;
import com.coderank.entity.pojo.QuestionCodeTemplate;
import com.coderank.entity.vo.judge.TestSubmitResultVO;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.backend.judge.JudgeMachineMapper;
import com.coderank.mapper.backend.question.QuestionCodeTemplateMapper;
import com.coderank.mapper.backend.question.QuestionMapper;
import com.coderank.properties.JudgeProperties;
import com.coderank.service.judge.CodeAssembler;
import com.coderank.service.judge.JudgeExecutor;
import com.coderank.service.judge.JudgeLimits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 做题页测试运行业务，随机挑选评测机且不落库。
 */
@Service
public class ProblemRunService {

    /** 机器状态：启用 */
    private static final int STATUS_ENABLED = 1;

    /** 判题模式：核心代码，需要用桩程序拼接完整程序 */
    private static final int JUDGE_MODE_CORE = 2;

    private final JudgeMachineMapper judgeMachineMapper;

    private final JudgeExecutor judgeExecutor;

    private final QuestionMapper questionMapper;

    private final QuestionCodeTemplateMapper codeTemplateMapper;

    private final CodeAssembler codeAssembler;

    private final JudgeProperties judgeProperties;

    public ProblemRunService(JudgeMachineMapper judgeMachineMapper,
                             JudgeExecutor judgeExecutor,
                             QuestionMapper questionMapper,
                             QuestionCodeTemplateMapper codeTemplateMapper,
                             CodeAssembler codeAssembler,
                             JudgeProperties judgeProperties) {
        this.judgeMachineMapper = judgeMachineMapper;
        this.judgeExecutor = judgeExecutor;
        this.questionMapper = questionMapper;
        this.codeTemplateMapper = codeTemplateMapper;
        this.codeAssembler = codeAssembler;
        this.judgeProperties = judgeProperties;
    }

    // 运行一次代码，每次请求随机挑一台启用的评测机
    public TestSubmitResultVO run(RunCodeDTO runCodeDTO) {
        LanguageEnum language = LanguageEnum.fromCode(runCodeDTO.getLanguage())
                .orElseThrow(() -> new BusinessException(ResultCodeEnum.BAD_REQUEST, "不支持的语言"));

        Question question = questionMapper.selectById(runCodeDTO.getQuestionId());
        if (question == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "题目不存在");
        }

        // 核心代码模式只提交函数体，需要先拼成可运行的完整程序
        String code = assembleCode(question, language, runCodeDTO.getCode());
        JudgeLimits limits = JudgeLimits.ofQuestion(
                question.getTimeLimit(), question.getMemoryLimit(), judgeProperties);

        JudgeMachine machine = pickRandomMachine();
        return judgeExecutor.execute(machine, language, code, runCodeDTO.getInput(), limits);
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
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "该题缺少“" + language.getCode() + "”语言的桩程序，无法运行");
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
}
