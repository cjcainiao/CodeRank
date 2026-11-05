package com.coderank.service.backend.question;

import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.question.QuestionCreateDTO;
import com.coderank.entity.dto.question.QuestionQueryDTO;
import com.coderank.entity.dto.question.QuestionUpdateDTO;
import com.coderank.entity.vo.question.QuestionVO;

/**
 * 后台题目管理业务。
 */
public interface QuestionService {

    // 分页查询题目
    PageResult<QuestionVO> pageQuestions(QuestionQueryDTO queryDTO);

    // 按 id 查询题目
    QuestionVO getQuestionById(Long id);

    // 新增题目
    Long createQuestion(QuestionCreateDTO createDTO);

    // 修改题目
    void updateQuestion(QuestionUpdateDTO updateDTO);

    // 删除题目
    void deleteQuestion(Long id);
}
