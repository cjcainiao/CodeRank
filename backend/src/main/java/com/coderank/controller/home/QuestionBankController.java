package com.coderank.controller.home;

import com.coderank.common.result.PageResult;
import com.coderank.common.result.Result;
import com.coderank.entity.dto.QuestionBankQueryDTO;
import com.coderank.entity.vo.QuestionBankItemVO;
import com.coderank.entity.vo.TagOptionVO;
import com.coderank.service.home.QuestionBankService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页题库接口。
 */
@RestController
@RequestMapping("/home/questions")
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    public QuestionBankController(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    // 分页查询已发布的题目
    @GetMapping
    public Result<PageResult<QuestionBankItemVO>> pageQuestions(QuestionBankQueryDTO queryDTO) {
        return Result.success(questionBankService.pageQuestions(queryDTO));
    }

    // 查询可筛选的标签
    @GetMapping("/tags")
    public Result<List<TagOptionVO>> listTagOptions() {
        return Result.success(questionBankService.listTagOptions());
    }
}
