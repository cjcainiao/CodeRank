package com.coderank.controller.backend.judge;

import com.coderank.common.result.Result;
import com.coderank.entity.dto.judge.TestSubmitDTO;
import com.coderank.entity.vo.judge.TestSubmitResultVO;
import com.coderank.service.backend.judge.JudgeTestSubmitService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台测试提交接口。
 */
@RestController
@RequestMapping("/backend/test-submits")
public class TestSubmitController {

    private final JudgeTestSubmitService judgeTestSubmitService;

    public TestSubmitController(JudgeTestSubmitService judgeTestSubmitService) {
        this.judgeTestSubmitService = judgeTestSubmitService;
    }

    // 指定评测机运行一次提交的代码
    @PostMapping
    public Result<TestSubmitResultVO> submit(@RequestBody @Valid TestSubmitDTO submitDTO) {
        return Result.success(judgeTestSubmitService.submit(submitDTO));
    }
}
