package com.coderank.service.problem.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.problem.SubmissionQueryDTO;
import com.coderank.entity.pojo.Question;
import com.coderank.entity.pojo.Submission;
import com.coderank.entity.vo.problem.SubmissionVO;
import com.coderank.mapper.backend.question.QuestionMapper;
import com.coderank.mapper.home.SubmissionMapper;
import com.coderank.service.problem.SubmissionQueryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 后台提交记录业务实现。
 */
@Service
public class SubmissionQueryServiceImpl implements SubmissionQueryService {

    /** 分页每页最大条数 */
    private static final long MAX_PAGE_SIZE = 100L;

    /** 排序方向：升序 */
    private static final String SORT_ASC = "asc";

    /** 排序字段：耗时 */
    private static final String SORT_FIELD_TIME_USED = "timeUsed";

    /** 排序字段：内存 */
    private static final String SORT_FIELD_MEMORY_USED = "memoryUsed";

    private final SubmissionMapper submissionMapper;

    private final QuestionMapper questionMapper;

    public SubmissionQueryServiceImpl(SubmissionMapper submissionMapper, QuestionMapper questionMapper) {
        this.submissionMapper = submissionMapper;
        this.questionMapper = questionMapper;
    }

    // 分页查询提交记录，回填题目标题
    @Override
    public PageResult<SubmissionVO> pageSubmissions(SubmissionQueryDTO queryDTO) {
        long pageNum = queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1L : queryDTO.getPageNum();
        long pageSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1
                ? 10L : Math.min(queryDTO.getPageSize(), MAX_PAGE_SIZE);

        // 仅接受 asc，其余按降序处理，避免拼接非法排序值
        boolean ascending = SORT_ASC.equalsIgnoreCase(queryDTO.getSortOrder());

        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<Submission>()
                .eq(queryDTO.getQuestionId() != null, Submission::getQuestionId, queryDTO.getQuestionId())
                .eq(StringUtils.hasText(queryDTO.getLanguage()), Submission::getLanguage, queryDTO.getLanguage())
                .eq(queryDTO.getStatus() != null, Submission::getStatus, queryDTO.getStatus());

        applySort(wrapper, queryDTO.getSortField(), ascending);

        Page<Submission> page = submissionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<SubmissionVO> records = toVOList(page.getRecords());
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    // 按白名单字段排序，未指定字段时按创建时间降序
    private void applySort(LambdaQueryWrapper<Submission> wrapper, String sortField, boolean ascending) {
        switch (sortField == null ? "" : sortField) {
            case SORT_FIELD_TIME_USED -> wrapper.orderBy(true, ascending, Submission::getTimeUsed);
            case SORT_FIELD_MEMORY_USED -> wrapper.orderBy(true, ascending, Submission::getMemoryUsed);
            default -> wrapper.orderBy(true, ascending, Submission::getCreateTime)
                    .orderByDesc(Submission::getId);
        }
    }

    // 批量回填题目标题，避免逐条查询
    private List<SubmissionVO> toVOList(List<Submission> submissions) {
        if (submissions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = submissions.stream()
                .map(Submission::getQuestionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> titleById = questionIds.isEmpty()
                ? Collections.emptyMap()
                : questionMapper.selectBatchIds(questionIds).stream()
                        .collect(Collectors.toMap(Question::getId, Question::getTitle));

        return submissions.stream()
                .map(submission -> toVO(submission, titleById.get(submission.getQuestionId())))
                .toList();
    }

    // 实体转视图对象
    private SubmissionVO toVO(Submission submission, String questionTitle) {
        return SubmissionVO.builder()
                .id(submission.getId())
                .questionId(submission.getQuestionId())
                .questionTitle(questionTitle)
                .language(submission.getLanguage())
                .status(submission.getStatus())
                .passedCount(submission.getPassedCount())
                .totalCount(submission.getTotalCount())
                .timeUsed(submission.getTimeUsed())
                .memoryUsed(submission.getMemoryUsed())
                .errorMessage(submission.getErrorMessage())
                .judgeMachineId(submission.getJudgeMachineId())
                .sourceCode(submission.getSourceCode())
                .createTime(submission.getCreateTime())
                .build();
    }
}
