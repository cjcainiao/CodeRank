package com.coderank.service.home;

import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.question.QuestionBankQueryDTO;
import com.coderank.entity.vo.question.QuestionBankItemVO;
import com.coderank.entity.vo.question.QuestionDetailVO;
import com.coderank.entity.vo.question.TagOptionVO;

import java.util.List;

/**
 * 首页题库业务。
 */
public interface QuestionBankService {

    // 分页查询已发布的题目
    PageResult<QuestionBankItemVO> pageQuestions(QuestionBankQueryDTO queryDTO);

    // 按 id 查询已发布的题目详情
    QuestionDetailVO getQuestionDetail(Long id);

    // 查询可筛选的标签
    List<TagOptionVO> listTagOptions();
}
