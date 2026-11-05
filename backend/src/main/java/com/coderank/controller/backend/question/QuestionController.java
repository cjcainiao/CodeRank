package com.coderank.controller.backend.question;

import com.coderank.common.result.PageResult;
import com.coderank.common.result.Result;
import com.coderank.entity.dto.question.QuestionCreateDTO;
import com.coderank.entity.dto.question.QuestionQueryDTO;
import com.coderank.entity.dto.question.QuestionUpdateDTO;
import com.coderank.entity.vo.question.QuestionVO;
import com.coderank.service.backend.question.QuestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台题目管理接口。
 */
@RestController
@RequestMapping("/backend/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // 分页查询题目
    @GetMapping
    public Result<PageResult<QuestionVO>> pageQuestions(QuestionQueryDTO queryDTO) {
        return Result.success(questionService.pageQuestions(queryDTO));
    }

    // 按 id 查询题目
    @GetMapping("/{id}")
    public Result<QuestionVO> getQuestionById(@PathVariable Long id) {
        return Result.success(questionService.getQuestionById(id));
    }

    // 新增题目，主键转字符串避免前端精度丢失
    @PostMapping
    public Result<String> createQuestion(@RequestBody @Valid QuestionCreateDTO createDTO) {
        return Result.success(String.valueOf(questionService.createQuestion(createDTO)));
    }

    // 修改题目
    @PutMapping
    public Result<Void> updateQuestion(@RequestBody @Valid QuestionUpdateDTO updateDTO) {
        questionService.updateQuestion(updateDTO);
        return Result.success();
    }

    // 删除题目
    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success();
    }
}
