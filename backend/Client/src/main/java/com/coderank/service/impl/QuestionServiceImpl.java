package com.coderank.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coderank.entity.dto.QuestionDTO;
import com.coderank.entity.pojo.Question;
import com.coderank.enums.ResultCode;
import com.coderank.exception.BusinessException;
import com.coderank.service.QuestionService;
import com.coderank.mapper.QuestionMapper;
import com.coderank.utils.ResponseResult;
import org.springframework.stereotype.Service;

/**
 * 题目相关业务实现
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {


    /**
     * 创建题目
     *
     * @param questionDTO
     * @return
     */
    public ResponseResult<Boolean> create(QuestionDTO questionDTO) {
        // 判断题目标题是否存在
        boolean exists = this.exists(new LambdaQueryWrapper<Question>().eq(Question::getTitle, questionDTO.getTitle()));
        if (exists) {
            throw new BusinessException(ResultCode.QUESTIONTITLE_ISUSE.getCode(), ResultCode.QUESTIONTITLE_ISUSE.getMsg());
        }

        // 保存题目
        Question question = BeanUtil.copyProperties(questionDTO, Question.class);
        boolean b = this.save(question);

        if (!b) {
            throw new BusinessException("创建失败");
        }
        return ResponseResult.success("创建成功");
    }
}




