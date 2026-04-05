package com.coderank.controller;

import com.coderank.entity.dto.QuestionDTO;
import com.coderank.entity.vo.QuestionVO;
import com.coderank.service.QuestionService;
import com.coderank.utils.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 题目相关操作
 */
@RestController
@RequestMapping("/question")
@Api(tags = "题目相关操作")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/create")
    @ApiOperation("创建题目")
    public ResponseResult<Boolean> create(@Validated(QuestionDTO.create.class) @RequestBody QuestionDTO questionDTO){
        return questionService.create(questionDTO);
    }

    //查询题目信息
    @GetMapping("/query/{id}")
    @ApiOperation("查询单道题目信息")
    public ResponseResult<QuestionVO> queryById(@PathVariable Long id){
        return questionService.queryById(id);
    }
}
