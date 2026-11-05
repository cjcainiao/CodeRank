package com.coderank.controller.backend;

import com.coderank.common.result.Result;
import com.coderank.entity.dto.QuestionTestDataCreateDTO;
import com.coderank.entity.dto.QuestionTestDataUpdateDTO;
import com.coderank.entity.vo.QuestionTestDataVO;
import com.coderank.service.backend.QuestionTestDataService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 后台题目评测数据管理接口。
 */
@RestController
@RequestMapping("/backend/question-test-data")
public class QuestionTestDataController {

    private final QuestionTestDataService testDataService;

    public QuestionTestDataController(QuestionTestDataService testDataService) {
        this.testDataService = testDataService;
    }

    // 查询题目下的全部评测数据
    @GetMapping("/question/{questionId}")
    public Result<List<QuestionTestDataVO>> listByQuestionId(@PathVariable Long questionId) {
        return Result.success(testDataService.listByQuestionId(questionId));
    }

    // 按 id 查询评测数据
    @GetMapping("/{id}")
    public Result<QuestionTestDataVO> getTestDataById(@PathVariable Long id) {
        return Result.success(testDataService.getTestDataById(id));
    }

    // 新增评测数据，主键转字符串避免前端精度丢失
    @PostMapping
    public Result<String> createTestData(@RequestBody @Valid QuestionTestDataCreateDTO createDTO) {
        return Result.success(String.valueOf(testDataService.createTestData(createDTO)));
    }

    // 上传指定题目的评测数据，input 与 output 两个文件成对提交
    @PostMapping("/question/{questionId}/upload")
    public Result<String> uploadTestData(@PathVariable Long questionId,
                                         @RequestParam("inputFile") MultipartFile inputFile,
                                         @RequestParam("outputFile") MultipartFile outputFile,
                                         @RequestParam(value = "visible", required = false) Integer visible) {
        return Result.success(String.valueOf(
                testDataService.uploadTestData(questionId, inputFile, outputFile, visible)));
    }

    // 修改评测数据
    @PutMapping
    public Result<Void> updateTestData(@RequestBody @Valid QuestionTestDataUpdateDTO updateDTO) {
        testDataService.updateTestData(updateDTO);
        return Result.success();
    }

    // 删除评测数据
    @DeleteMapping("/{id}")
    public Result<Void> deleteTestData(@PathVariable Long id) {
        testDataService.deleteTestData(id);
        return Result.success();
    }
}
