package com.coderank.controller.backend;

import com.coderank.common.result.Result;
import com.coderank.entity.dto.QuestionTestCaseCreateDTO;
import com.coderank.entity.dto.QuestionTestCaseUpdateDTO;
import com.coderank.entity.vo.QuestionTestCaseVO;
import com.coderank.service.backend.QuestionTestCaseService;
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
 * 后台题目测试用例管理接口。
 */
@RestController
@RequestMapping("/backend/question-test-cases")
public class QuestionTestCaseController {

    private final QuestionTestCaseService testCaseService;

    public QuestionTestCaseController(QuestionTestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    // 查询题目下的全部测试用例
    @GetMapping("/question/{questionId}")
    public Result<List<QuestionTestCaseVO>> listByQuestionId(@PathVariable Long questionId) {
        return Result.success(testCaseService.listByQuestionId(questionId));
    }

    // 按 id 查询测试用例
    @GetMapping("/{id}")
    public Result<QuestionTestCaseVO> getTestCaseById(@PathVariable Long id) {
        return Result.success(testCaseService.getTestCaseById(id));
    }

    // 新增测试用例，主键转字符串避免前端精度丢失
    @PostMapping
    public Result<String> createTestCase(@RequestBody @Valid QuestionTestCaseCreateDTO createDTO) {
        return Result.success(String.valueOf(testCaseService.createTestCase(createDTO)));
    }

    // 修改测试用例
    @PutMapping
    public Result<Void> updateTestCase(@RequestBody @Valid QuestionTestCaseUpdateDTO updateDTO) {
        testCaseService.updateTestCase(updateDTO);
        return Result.success();
    }

    // 删除测试用例
    @DeleteMapping("/{id}")
    public Result<Void> deleteTestCase(@PathVariable Long id) {
        testCaseService.deleteTestCase(id);
        return Result.success();
    }
}
