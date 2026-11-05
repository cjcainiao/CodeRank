package com.coderank.controller.backend;

import com.coderank.common.result.Result;
import com.coderank.entity.dto.QuestionCodeTemplateCreateDTO;
import com.coderank.entity.dto.QuestionCodeTemplateUpdateDTO;
import com.coderank.entity.vo.QuestionCodeTemplateVO;
import com.coderank.service.backend.QuestionCodeTemplateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台题目代码模板管理接口。
 */
@RestController
@RequestMapping("/backend/question-code-templates")
public class QuestionCodeTemplateController {

    private final QuestionCodeTemplateService templateService;

    public QuestionCodeTemplateController(QuestionCodeTemplateService templateService) {
        this.templateService = templateService;
    }

    // 查询题目下的全部代码模板
    @GetMapping("/question/{questionId}")
    public Result<List<QuestionCodeTemplateVO>> listByQuestionId(@PathVariable Long questionId) {
        return Result.success(templateService.listByQuestionId(questionId));
    }

    // 按 id 查询代码模板
    @GetMapping("/{id}")
    public Result<QuestionCodeTemplateVO> getTemplateById(@PathVariable Long id) {
        return Result.success(templateService.getTemplateById(id));
    }

    // 新增代码模板，主键转字符串避免前端精度丢失
    @PostMapping
    public Result<String> createTemplate(@RequestBody @Valid QuestionCodeTemplateCreateDTO createDTO) {
        return Result.success(String.valueOf(templateService.createTemplate(createDTO)));
    }

    // 修改代码模板
    @PutMapping
    public Result<Void> updateTemplate(@RequestBody @Valid QuestionCodeTemplateUpdateDTO updateDTO) {
        templateService.updateTemplate(updateDTO);
        return Result.success();
    }

    // 删除代码模板
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return Result.success();
    }
}
