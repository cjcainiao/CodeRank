package com.coderank.controller.backend.question;

import com.coderank.common.result.PageResult;
import com.coderank.common.result.Result;
import com.coderank.entity.dto.problem.SubmissionQueryDTO;
import com.coderank.entity.vo.problem.SubmissionVO;
import com.coderank.service.problem.SubmissionQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台提交记录管理接口。
 */
@RestController
@RequestMapping("/backend/submissions")
public class SubmissionController {

    private final SubmissionQueryService submissionQueryService;

    public SubmissionController(SubmissionQueryService submissionQueryService) {
        this.submissionQueryService = submissionQueryService;
    }

    // 分页查询提交记录
    @GetMapping
    public Result<PageResult<SubmissionVO>> pageSubmissions(SubmissionQueryDTO queryDTO) {
        return Result.success(submissionQueryService.pageSubmissions(queryDTO));
    }
}
