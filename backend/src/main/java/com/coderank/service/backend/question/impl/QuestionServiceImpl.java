package com.coderank.service.backend.question.impl;

import com.coderank.common.enums.ResultCodeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.question.QuestionCreateDTO;
import com.coderank.entity.dto.question.QuestionQueryDTO;
import com.coderank.entity.dto.question.QuestionUpdateDTO;
import com.coderank.entity.pojo.Question;
import com.coderank.entity.pojo.QuestionCodeTemplate;
import com.coderank.entity.pojo.QuestionTag;
import com.coderank.entity.pojo.QuestionTestCase;
import com.coderank.entity.pojo.QuestionTestData;
import com.coderank.entity.vo.question.QuestionVO;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.backend.question.QuestionCodeTemplateMapper;
import com.coderank.mapper.backend.question.QuestionMapper;
import com.coderank.mapper.backend.question.QuestionTagMapper;
import com.coderank.mapper.backend.question.QuestionTestCaseMapper;
import com.coderank.mapper.backend.question.QuestionTestDataMapper;
import com.coderank.properties.FileUploadProperties;
import com.coderank.service.backend.question.QuestionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 后台题目管理业务实现。
 */
@Service
public class QuestionServiceImpl implements QuestionService {

    /** 分页每页最大条数 */
    private static final long MAX_PAGE_SIZE = 100L;

    /** 排序方向：升序 */
    private static final String SORT_ASC = "asc";

    /** 排序字段：创建时间 */
    private static final String SORT_FIELD_CREATE_TIME = "createTime";

    /** 排序字段：更新时间 */
    private static final String SORT_FIELD_UPDATE_TIME = "updateTime";

    /** 难度：简单 */
    private static final int DIFFICULTY_EASY = 1;

    /** 判题模式：标准输入输出 */
    private static final int JUDGE_MODE_STANDARD = 1;

    /** 状态：草稿 */
    private static final int STATUS_DRAFT = 0;

    /** 默认时间限制，毫秒 */
    private static final int DEFAULT_TIME_LIMIT = 1000;

    /** 默认内存限制，MB */
    private static final int DEFAULT_MEMORY_LIMIT = 128;

    /** 空样例 */
    private static final String EMPTY_SAMPLE_CASES = "[]";

    private final QuestionMapper questionMapper;

    private final QuestionTagMapper questionTagMapper;

    private final QuestionCodeTemplateMapper codeTemplateMapper;

    private final QuestionTestCaseMapper testCaseMapper;

    private final QuestionTestDataMapper testDataMapper;

    private final FileUploadProperties fileUploadProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuestionServiceImpl(QuestionMapper questionMapper,
                               QuestionTagMapper questionTagMapper,
                               QuestionCodeTemplateMapper codeTemplateMapper,
                               QuestionTestCaseMapper testCaseMapper,
                               QuestionTestDataMapper testDataMapper,
                               FileUploadProperties fileUploadProperties) {
        this.questionMapper = questionMapper;
        this.questionTagMapper = questionTagMapper;
        this.codeTemplateMapper = codeTemplateMapper;
        this.testCaseMapper = testCaseMapper;
        this.testDataMapper = testDataMapper;
        this.fileUploadProperties = fileUploadProperties;
    }

    // 分页查询题目
    @Override
    public PageResult<QuestionVO> pageQuestions(QuestionQueryDTO queryDTO) {
        long pageNum = queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1L : queryDTO.getPageNum();
        long pageSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1
                ? 10L : Math.min(queryDTO.getPageSize(), MAX_PAGE_SIZE);

        // 仅接受 asc，其余按降序处理，避免拼接非法排序值
        boolean ascending = SORT_ASC.equalsIgnoreCase(queryDTO.getSortOrder());

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .like(StringUtils.hasText(queryDTO.getTitle()), Question::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getDifficulty() != null, Question::getDifficulty, queryDTO.getDifficulty())
                .eq(queryDTO.getJudgeMode() != null, Question::getJudgeMode, queryDTO.getJudgeMode())
                .eq(queryDTO.getStatus() != null, Question::getStatus, queryDTO.getStatus())
                .like(StringUtils.hasText(queryDTO.getSource()), Question::getSource, queryDTO.getSource());

        applySort(wrapper, queryDTO.getSortField(), ascending);

        Page<Question> page = questionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<QuestionVO> records = toVOList(page.getRecords());
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    // 批量回填当前页题目的标签，避免逐条查询
    private List<QuestionVO> toVOList(List<Question> questions) {
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = questions.stream().map(Question::getId).toList();
        List<QuestionTag> relations = questionTagMapper.selectList(
                new LambdaQueryWrapper<QuestionTag>().in(QuestionTag::getQuestionId, questionIds));

        Map<Long, List<String>> tagIdsByQuestion = relations.stream().collect(Collectors.groupingBy(
                QuestionTag::getQuestionId,
                Collectors.mapping(relation -> String.valueOf(relation.getTagId()), Collectors.toList())));

        return questions.stream()
                .map(question -> toVO(question,
                        tagIdsByQuestion.getOrDefault(question.getId(), Collections.emptyList())))
                .toList();
    }

    // 按白名单字段排序，未知字段回退到 id
    private void applySort(LambdaQueryWrapper<Question> wrapper, String sortField, boolean ascending) {
        switch (sortField == null ? "" : sortField) {
            case SORT_FIELD_CREATE_TIME -> wrapper.orderBy(true, ascending, Question::getCreateTime);
            case SORT_FIELD_UPDATE_TIME -> wrapper.orderBy(true, ascending, Question::getUpdateTime);
            default -> wrapper.orderBy(true, ascending, Question::getId);
        }
    }

    // 按 id 查询题目
    @Override
    public QuestionVO getQuestionById(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "题目不存在");
        }
        return toVOList(List.of(question)).get(0);
    }

    // 新增题目
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestion(QuestionCreateDTO createDTO) {
        String sampleCases = resolveSampleCases(createDTO.getSampleCases());

        Question question = Question.builder()
                .title(createDTO.getTitle())
                .difficulty(createDTO.getDifficulty() == null ? DIFFICULTY_EASY : createDTO.getDifficulty())
                .judgeMode(createDTO.getJudgeMode() == null ? JUDGE_MODE_STANDARD : createDTO.getJudgeMode())
                .description(createDTO.getDescription())
                .inputFormat(createDTO.getInputFormat())
                .outputFormat(createDTO.getOutputFormat())
                .sampleCases(sampleCases)
                .hint(createDTO.getHint())
                .timeLimit(createDTO.getTimeLimit() == null ? DEFAULT_TIME_LIMIT : createDTO.getTimeLimit())
                .memoryLimit(createDTO.getMemoryLimit() == null ? DEFAULT_MEMORY_LIMIT : createDTO.getMemoryLimit())
                .source(createDTO.getSource())
                .status(createDTO.getStatus() == null ? STATUS_DRAFT : createDTO.getStatus())
                .submitCount(0)
                .acceptedCount(0)
                .build();

        questionMapper.insert(question);
        saveQuestionTags(question.getId(), createDTO.getTagIds());
        return question.getId();
    }

    // 修改题目
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestion(QuestionUpdateDTO updateDTO) {
        Question existing = questionMapper.selectById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "题目不存在");
        }

        Question question = Question.builder()
                .id(updateDTO.getId())
                .title(updateDTO.getTitle())
                .difficulty(updateDTO.getDifficulty())
                .judgeMode(updateDTO.getJudgeMode())
                .description(updateDTO.getDescription())
                .inputFormat(updateDTO.getInputFormat())
                .outputFormat(updateDTO.getOutputFormat())
                .sampleCases(updateDTO.getSampleCases())
                .hint(updateDTO.getHint())
                .timeLimit(updateDTO.getTimeLimit())
                .memoryLimit(updateDTO.getMemoryLimit())
                .source(updateDTO.getSource())
                .status(updateDTO.getStatus())
                .build();
        // 未传样例时保持原值，传了才校验并覆盖
        if (updateDTO.getSampleCases() != null) {
            question.setSampleCases(resolveSampleCases(updateDTO.getSampleCases()));
        }
        questionMapper.updateById(question);

        // 未传标签列表时保持原有关联，传空数组表示清空
        if (updateDTO.getTagIds() != null) {
            replaceQuestionTags(updateDTO.getId(), updateDTO.getTagIds());
        }
    }

    // 删除题目
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long id) {
        if (questionMapper.selectById(id) == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "题目不存在");
        }
        questionMapper.deleteById(id);
        questionTagMapper.delete(new LambdaQueryWrapper<QuestionTag>()
                .eq(QuestionTag::getQuestionId, id));
        codeTemplateMapper.delete(new LambdaQueryWrapper<QuestionCodeTemplate>()
                .eq(QuestionCodeTemplate::getQuestionId, id));
        testCaseMapper.delete(new LambdaQueryWrapper<QuestionTestCase>()
                .eq(QuestionTestCase::getQuestionId, id));
        testDataMapper.delete(new LambdaQueryWrapper<QuestionTestData>()
                .eq(QuestionTestData::getQuestionId, id));
        deleteTestDataDirectory(id);
    }

    // 删除题目的评测数据目录，避免磁盘残留
    private void deleteTestDataDirectory(Long questionId) {
        FileUploadProperties.TestData config = fileUploadProperties.getTestData();
        Path directory = Paths.get(fileUploadProperties.getRoot(), config.getDir(), String.valueOf(questionId));
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            // 逆序删除，先删文件再删目录
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // 单个文件删除失败不阻断其余文件，残留由运维处理
                }
            });
        } catch (IOException ignored) {
            // 目录遍历失败时保留数据库已删除的结果，残留由运维处理
        }
    }

    // 保存题目标签关联，重复的标签 id 只保留一个
    private void saveQuestionTags(Long questionId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        tagIds.stream().distinct().forEach(tagId -> {
            QuestionTag relation = QuestionTag.builder()
                    .questionId(questionId)
                    .tagId(tagId)
                    .build();
            questionTagMapper.insert(relation);
        });
    }

    // 覆盖题目标签关联，先清空再写入
    private void replaceQuestionTags(Long questionId, List<Long> tagIds) {
        questionTagMapper.delete(new LambdaQueryWrapper<QuestionTag>()
                .eq(QuestionTag::getQuestionId, questionId));
        saveQuestionTags(questionId, tagIds);
    }

    // 校验样例为合法 JSON 数组，未传时返回空数组
    private String resolveSampleCases(String sampleCases) {
        if (!StringUtils.hasText(sampleCases)) {
            return EMPTY_SAMPLE_CASES;
        }
        try {
            if (!objectMapper.readTree(sampleCases).isArray()) {
                throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "样例须为 JSON 数组");
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "样例不是合法的 JSON");
        }
        return sampleCases;
    }

    // 实体转视图对象
    private QuestionVO toVO(Question question, List<String> tagIds) {
        return QuestionVO.builder()
                .id(question.getId())
                .title(question.getTitle())
                .difficulty(question.getDifficulty())
                .judgeMode(question.getJudgeMode())
                .description(question.getDescription())
                .inputFormat(question.getInputFormat())
                .outputFormat(question.getOutputFormat())
                .sampleCases(question.getSampleCases())
                .hint(question.getHint())
                .timeLimit(question.getTimeLimit())
                .memoryLimit(question.getMemoryLimit())
                .source(question.getSource())
                .status(question.getStatus())
                .submitCount(question.getSubmitCount())
                .acceptedCount(question.getAcceptedCount())
                .tagIds(tagIds)
                .createTime(question.getCreateTime())
                .updateTime(question.getUpdateTime())
                .build();
    }
}
