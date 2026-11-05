package com.coderank.mapper.backend.question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderank.entity.pojo.QuestionTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目标签关联数据访问。
 */
@Mapper
public interface QuestionTagMapper extends BaseMapper<QuestionTag> {
}
