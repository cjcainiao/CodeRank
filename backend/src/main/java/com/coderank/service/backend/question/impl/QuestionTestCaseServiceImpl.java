package com.coderank.service.backend.question.impl;

import com.coderank.common.enums.ResultCodeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coderank.entity.dto.question.QuestionTestCaseCreateDTO;
import com.coderank.entity.dto.question.QuestionTestCaseUpdateDTO;
import com.coderank.entity.pojo.Question;
import com.coderank.entity.pojo.QuestionTestCase;
import com.coderank.entity.vo.question.QuestionTestCaseVO;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.backend.question.QuestionMapper;
import com.coderank.mapper.backend.question.QuestionTestCaseMapper;
import com.coderank.service.backend.question.QuestionTestCaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 后台题目测试用例管理业务实现。
 */
@Service
public class QuestionTestCaseServiceImpl implements QuestionTestCaseService {

    /** 是否展示：展示 */
    private static final int VISIBLE = 1;

    /** 默认排序值 */
    private static final int DEFAULT_SORT_ORDER = 0;

    /** 判题模式：核心代码 */
    private static final int JUDGE_MODE_CORE = 2;

    private final QuestionTestCaseMapper testCaseMapper;

    private final QuestionMapper questionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuestionTestCaseServiceImpl(QuestionTestCaseMapper testCaseMapper, QuestionMapper questionMapper) {
        this.testCaseMapper = testCaseMapper;
        this.questionMapper = questionMapper;
    }

    // 查询题目下的全部测试用例，按排序值升序
    @Override
    public List<QuestionTestCaseVO> listByQuestionId(Long questionId) {
        if (questionMapper.selectById(questionId) == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "题目不存在");
        }
        List<QuestionTestCase> testCases = testCaseMapper.selectList(
                new LambdaQueryWrapper<QuestionTestCase>()
                        .eq(QuestionTestCase::getQuestionId, questionId)
                        .orderByAsc(QuestionTestCase::getSortOrder)
                        .orderByAsc(QuestionTestCase::getId));
        return testCases.stream().map(this::toVO).toList();
    }

    // 按 id 查询测试用例
    @Override
    public QuestionTestCaseVO getTestCaseById(Long id) {
        QuestionTestCase testCase = testCaseMapper.selectById(id);
        if (testCase == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "测试用例不存在");
        }
        return toVO(testCase);
    }

    // 新增测试用例
    @Override
    public Long createTestCase(QuestionTestCaseCreateDTO createDTO) {
        Question question = questionMapper.selectById(createDTO.getQuestionId());
        if (question == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "题目不存在");
        }

        QuestionTestCase testCase = QuestionTestCase.builder()
                .questionId(createDTO.getQuestionId())
                .visible(createDTO.getVisible() == null ? VISIBLE : createDTO.getVisible())
                .sortOrder(createDTO.getSortOrder() == null ? DEFAULT_SORT_ORDER : createDTO.getSortOrder())
                .build();
        fillCaseContent(testCase, question.getJudgeMode(),
                createDTO.getArgs(), createDTO.getExpected(),
                createDTO.getInputData(), createDTO.getExpectedOut());

        testCaseMapper.insert(testCase);
        return testCase.getId();
    }

    // 修改测试用例
    @Override
    public void updateTestCase(QuestionTestCaseUpdateDTO updateDTO) {
        QuestionTestCase existing = testCaseMapper.selectById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "测试用例不存在");
        }
        Question question = questionMapper.selectById(existing.getQuestionId());
        if (question == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "题目不存在");
        }

        QuestionTestCase testCase = QuestionTestCase.builder()
                .id(updateDTO.getId())
                .visible(updateDTO.getVisible())
                .sortOrder(updateDTO.getSortOrder())
                .build();
        // 未传的字段保持原值，传了才校验并覆盖
        if (updateDTO.getArgs() != null || updateDTO.getExpected() != null
                || updateDTO.getInputData() != null || updateDTO.getExpectedOut() != null) {
            fillCaseContent(testCase, question.getJudgeMode(),
                    updateDTO.getArgs(), updateDTO.getExpected(),
                    updateDTO.getInputData(), updateDTO.getExpectedOut());
        }
        testCaseMapper.updateById(testCase);
    }

    // 删除测试用例
    @Override
    public void deleteTestCase(Long id) {
        if (testCaseMapper.selectById(id) == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "测试用例不存在");
        }
        testCaseMapper.deleteById(id);
    }

    // 按判题模式写入对应的用例内容，两种模式只保留各自字段
    private void fillCaseContent(QuestionTestCase testCase, Integer judgeMode,
                                 String args, String expected,
                                 String inputData, String expectedOut) {
        if (judgeMode != null && judgeMode == JUDGE_MODE_CORE) {
            if (!StringUtils.hasText(args) || !StringUtils.hasText(expected)) {
                throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "核心代码模式须填写函数入参与期望返回值");
            }
            testCase.setArgs(resolveArgs(args));
            testCase.setExpected(resolveExpected(expected));
            testCase.setInputData(null);
            testCase.setExpectedOut(null);
            return;
        }

        if (!StringUtils.hasText(inputData) || !StringUtils.hasText(expectedOut)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "标准输入输出模式须填写标准输入与标准输出");
        }
        testCase.setArgs(null);
        testCase.setExpected(null);
        testCase.setInputData(inputData);
        testCase.setExpectedOut(expectedOut);
    }

    // 校验入参为合法 JSON 数组
    private String resolveArgs(String args) {
        try {
            if (!objectMapper.readTree(args).isArray()) {
                throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "函数入参须为 JSON 数组");
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "函数入参不是合法的 JSON");
        }
        return args;
    }

    // 校验期望值为合法 JSON
    private String resolveExpected(String expected) {
        try {
            objectMapper.readTree(expected);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "期望返回值不是合法的 JSON");
        }
        return expected;
    }

    // 实体转视图对象
    private QuestionTestCaseVO toVO(QuestionTestCase testCase) {
        return QuestionTestCaseVO.builder()
                .id(testCase.getId())
                .questionId(testCase.getQuestionId())
                .args(testCase.getArgs())
                .expected(testCase.getExpected())
                .inputData(testCase.getInputData())
                .expectedOut(testCase.getExpectedOut())
                .visible(testCase.getVisible())
                .sortOrder(testCase.getSortOrder())
                .createTime(testCase.getCreateTime())
                .updateTime(testCase.getUpdateTime())
                .build();
    }
}
