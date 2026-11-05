package com.coderank.service.home;

import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.QuestionBankQueryDTO;
import com.coderank.entity.vo.QuestionBankItemVO;
import com.coderank.entity.vo.TagOptionVO;

import java.util.List;

/**
 * 首页题库业务。
 */
public interface QuestionBankService {

    // 分页查询已发布的题目
    PageResult<QuestionBankItemVO> pageQuestions(QuestionBankQueryDTO queryDTO);

    // 查询可筛选的标签
    List<TagOptionVO> listTagOptions();
}
