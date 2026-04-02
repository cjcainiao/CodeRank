package com.coderank.controller;

import com.coderank.entity.dto.TaskDTO;
import com.coderank.entity.vo.QuestionSubmitVO;
import com.coderank.entity.vo.TaskVO;
import com.coderank.service.QuestionSubmitService;
import com.coderank.utils.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    //查询任务接口
    @GetMapping("/findTask")
    @ApiOperation("查询任务接口")
    public ResponseResult<TaskVO> findTask(Long id){
        return taskSubmitService.findTask(id);
    }
}
