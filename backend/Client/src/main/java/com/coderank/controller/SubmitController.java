package com.coderank.controller;

import com.coderank.entity.dto.TaskDTO;
import com.coderank.entity.vo.QuestionSubmitVO;
import com.coderank.service.QuestionSubmitService;
import com.coderank.utils.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提交代码接口
 */
@RestController
@RequestMapping("/submit")
@Api(tags = "提交代码相关接口")
@RequiredArgsConstructor
public class SubmitController {

    private final QuestionSubmitService taskSubmitService;

    @PostMapping("/task")
    @ApiOperation("评测任务接口")
    public ResponseResult<QuestionSubmitVO> Task(@Validated @RequestBody TaskDTO taskDTO){
        return taskSubmitService.task(taskDTO);
    }
}
