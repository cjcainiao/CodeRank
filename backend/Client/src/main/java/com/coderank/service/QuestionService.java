package com.coderank.service;

import com.coderank.entity.dto.QuestionDTO;
import com.coderank.entity.pojo.Question;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coderank.utils.ResponseResult;

/**
 * 题目相关业务接口
 */
public interface QuestionService extends IService<Question> {

    /**
     * 创建题目
     * @param questionDTO
     * @return
     */
    ResponseResult<Boolean> create(QuestionDTO questionDTO);
}
