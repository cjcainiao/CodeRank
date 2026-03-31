package com.coderank.controller;

import com.coderank.entity.dto.QuestionDTO;
import com.coderank.service.QuestionService;
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
}
