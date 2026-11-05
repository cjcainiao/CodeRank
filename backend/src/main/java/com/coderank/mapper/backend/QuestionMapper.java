package com.coderank.mapper.backend;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderank.entity.pojo.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台题目数据访问。
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
