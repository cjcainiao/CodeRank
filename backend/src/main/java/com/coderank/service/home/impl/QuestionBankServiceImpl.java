package com.coderank.service.home.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.question.QuestionBankQueryDTO;
import com.coderank.entity.pojo.Question;
import com.coderank.entity.pojo.QuestionCodeTemplate;
import com.coderank.entity.pojo.QuestionTag;
import com.coderank.entity.pojo.Tag;
import com.coderank.entity.vo.question.QuestionBankItemVO;
import com.coderank.entity.vo.question.QuestionDetailVO;
import com.coderank.entity.vo.question.TagOptionVO;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.backend.question.QuestionCodeTemplateMapper;
import com.coderank.mapper.backend.question.QuestionMapper;
import com.coderank.mapper.backend.question.QuestionTagMapper;
import com.coderank.mapper.backend.question.TagMapper;


import com.coderank.service.home.QuestionBankService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页题库业务实现。
 */
@Service
public class QuestionBankServiceImpl implements QuestionBankService {

    /** 分页每页最大条数 */
    private static final long MAX_PAGE_SIZE = 100L;

    /** 排序方向：升序 */
    private static final String SORT_ASC = "asc";

    /** 排序字段：提交次数 */
    private static final String SORT_FIELD_SUBMIT_COUNT = "submitCount";

    /** 排序字段：通过次数 */
    private static final String SORT_FIELD_ACCEPTED_COUNT = "acceptedCount";

    /** 题目状态：已发布 */
    private static final int STATUS_PUBLISHED = 1;

    private final QuestionMapper questionMapper;

    private final QuestionTagMapper questionTagMapper;

    private final TagMapper tagMapper;

    private final QuestionCodeTemplateMapper codeTemplateMapper;

    public QuestionBankServiceImpl(QuestionMapper questionMapper,
                                   QuestionTagMapper questionTagMapper,
                                   TagMapper tagMapper,
                                   QuestionCodeTemplateMapper codeTemplateMapper) {
        this.questionMapper = questionMapper;
        this.questionTagMapper = questionTagMapper;
        this.tagMapper = tagMapper;
        this.codeTemplateMapper = codeTemplateMapper;
    }

    // 分页查询已发布的题目
    @Override
    public PageResult<QuestionBankItemVO> pageQuestions(QuestionBankQueryDTO queryDTO) {
        long pageNum = queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1L : queryDTO.getPageNum();
        long pageSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1
                ? 20L : Math.min(queryDTO.getPageSize(), MAX_PAGE_SIZE);

        // 仅接受 asc，其余按降序处理，避免拼接非法排序值
        boolean ascending = SORT_ASC.equalsIgnoreCase(queryDTO.getSortOrder());

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getStatus, STATUS_PUBLISHED)
                .like(StringUtils.hasText(queryDTO.getTitle()), Question::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getDifficulty() != null, Question::getDifficulty, queryDTO.getDifficulty());

        // 按标签筛选时先取该标签下的题目 id，再作为查询条件
        if (queryDTO.getTagId() != null) {
            List<Long> questionIds = questionTagMapper.selectList(
                            new LambdaQueryWrapper<QuestionTag>()
                                    .eq(QuestionTag::getTagId, queryDTO.getTagId()))
                    .stream()
                    .map(QuestionTag::getQuestionId)
                    .toList();
            if (questionIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L, pageNum, pageSize);
            }
            wrapper.in(Question::getId, questionIds);
        }

        applySort(wrapper, queryDTO.getSortField(), ascending);

        Page<Question> page = questionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(toVOList(page.getRecords()), page.getTotal(), page.getCurrent(), page.getSize());
    }

    // 按白名单字段排序，未知字段回退到 id
    private void applySort(LambdaQueryWrapper<Question> wrapper, String sortField, boolean ascending) {
        switch (sortField == null ? "" : sortField) {
            case SORT_FIELD_SUBMIT_COUNT -> wrapper.orderBy(true, ascending, Question::getSubmitCount);
            case SORT_FIELD_ACCEPTED_COUNT -> wrapper.orderBy(true, ascending, Question::getAcceptedCount);
            default -> wrapper.orderBy(true, ascending, Question::getId);
        }
    }

    // 按 id 查询已发布的题目详情
    @Override
    public QuestionDetailVO getQuestionDetail(Long id) {
        Question question = questionMapper.selectById(id);
        // 草稿与下架题不对用户开放
        if (question == null || question.getStatus() == null || question.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException(404, "题目不存在");
        }

        QuestionDetailVO vo = new QuestionDetailVO();
        BeanUtils.copyProperties(question, vo);
        vo.setTags(listTagNames(id));

        List<QuestionCodeTemplate> templates = codeTemplateMapper.selectList(
                new LambdaQueryWrapper<QuestionCodeTemplate>()
                        .eq(QuestionCodeTemplate::getQuestionId, id)
                        .orderByAsc(QuestionCodeTemplate::getLanguage));
        vo.setCodeTemplates(templates.stream().map(template -> {
            QuestionDetailVO.CodeTemplate item = new QuestionDetailVO.CodeTemplate();
            item.setLanguage(template.getLanguage());
            item.setTemplate(template.getTemplate());
            return item;
        }).toList());

        return vo;
    }

    // 查询题目挂载的标签名
    private List<String> listTagNames(Long questionId) {
        List<QuestionTag> relations = questionTagMapper.selectList(
                new LambdaQueryWrapper<QuestionTag>().eq(QuestionTag::getQuestionId, questionId));
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        return tagMapper.selectBatchIds(relations.stream().map(QuestionTag::getTagId).distinct().toList())
                .stream()
                .map(Tag::getName)
                .toList();
    }

    // 查询可筛选的标签，按排序值升序
    @Override
    public List<TagOptionVO> listTagOptions() {
        List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .orderByAsc(Tag::getSortOrder)
                .orderByAsc(Tag::getId));
        return tags.stream().map(tag -> {
            TagOptionVO vo = new TagOptionVO();
            BeanUtils.copyProperties(tag, vo);
            return vo;
        }).toList();
    }

    // 批量回填当前页题目的标签名，避免逐条查询
    private List<QuestionBankItemVO> toVOList(List<Question> questions) {
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = questions.stream().map(Question::getId).toList();
        List<QuestionTag> relations = questionTagMapper.selectList(
                new LambdaQueryWrapper<QuestionTag>().in(QuestionTag::getQuestionId, questionIds));

        // 一次查出关联到的全部标签，再按题目分组
        Map<Long, String> tagNames = relations.isEmpty()
                ? Collections.emptyMap()
                : tagMapper.selectBatchIds(relations.stream().map(QuestionTag::getTagId).distinct().toList())
                        .stream()
                        .collect(Collectors.toMap(Tag::getId, Tag::getName));

        Map<Long, List<String>> namesByQuestion = relations.stream()
                .filter(relation -> tagNames.containsKey(relation.getTagId()))
                .collect(Collectors.groupingBy(
                        QuestionTag::getQuestionId,
                        Collectors.mapping(relation -> tagNames.get(relation.getTagId()), Collectors.toList())));

        return questions.stream().map(question -> {
            QuestionBankItemVO vo = new QuestionBankItemVO();
            BeanUtils.copyProperties(question, vo);
            vo.setTags(namesByQuestion.getOrDefault(question.getId(), Collections.emptyList()));
            return vo;
        }).toList();
    }
}
