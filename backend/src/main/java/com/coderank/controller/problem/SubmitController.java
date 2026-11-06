package com.coderank.controller.problem;

import com.coderank.common.result.Result;
import com.coderank.entity.dto.problem.SubmitCodeDTO;
import com.coderank.entity.vo.problem.SubmissionStatusVO;
import com.coderank.service.problem.ProblemJudgeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 做题页提交评测接口。
 */
@RestController
@RequestMapping("/problem/submit")
public class SubmitController {

    private final ProblemJudgeService problemJudgeService;

    public SubmitController(ProblemJudgeService problemJudgeService) {
        this.problemJudgeService = problemJudgeService;
    }

    // 提交代码，立即返回任务 id，评测在后台异步执行
    @PostMapping
    public Result<String> submit(@RequestBody @Valid SubmitCodeDTO submitDTO) {
        return Result.success(String.valueOf(problemJudgeService.submit(submitDTO)));
    }

    // 查询评测任务状态，前端按间隔轮询
    @GetMapping("/{submissionId}")
    public Result<SubmissionStatusVO> getStatus(@PathVariable Long submissionId) {
        return Result.success(problemJudgeService.getSubmissionStatus(submissionId));
    }
}
