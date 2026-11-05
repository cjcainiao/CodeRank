package com.coderank.service.backend.question;

import com.coderank.entity.dto.question.QuestionCodeTemplateCreateDTO;
import com.coderank.entity.dto.question.QuestionCodeTemplateUpdateDTO;
import com.coderank.entity.vo.question.QuestionCodeTemplateVO;

import java.util.List;

/**
 * 后台题目代码模板管理业务。
 */
public interface QuestionCodeTemplateService {

    // 查询题目下的全部代码模板
    List<QuestionCodeTemplateVO> listByQuestionId(Long questionId);

    // 按 id 查询代码模板
    QuestionCodeTemplateVO getTemplateById(Long id);

    // 新增代码模板
    Long createTemplate(QuestionCodeTemplateCreateDTO createDTO);

    // 修改代码模板
    void updateTemplate(QuestionCodeTemplateUpdateDTO updateDTO);

    // 删除代码模板
    void deleteTemplate(Long id);
}
