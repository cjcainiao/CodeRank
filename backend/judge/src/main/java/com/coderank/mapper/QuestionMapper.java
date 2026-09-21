package com.coderank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderank.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目数据访问层。
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
