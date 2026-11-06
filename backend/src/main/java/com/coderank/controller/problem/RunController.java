package com.coderank.controller.problem;

import com.coderank.common.result.Result;
import com.coderank.entity.dto.problem.RunCodeDTO;
import com.coderank.entity.vo.judge.TestSubmitResultVO;
import com.coderank.service.problem.ProblemRunService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 做题页代码运行接口。
 */
@RestController
@RequestMapping("/problem/run")
public class RunController {

    private final ProblemRunService problemRunService;

    public RunController(ProblemRunService problemRunService) {
        this.problemRunService = problemRunService;
    }

    // 运行一次代码，结果不落库
    @PostMapping
    public Result<TestSubmitResultVO> run(@RequestBody @Valid RunCodeDTO runCodeDTO) {
        return Result.success(problemRunService.run(runCodeDTO));
    }
}
