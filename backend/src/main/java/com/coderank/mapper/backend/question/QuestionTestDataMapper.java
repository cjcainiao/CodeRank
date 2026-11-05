package com.coderank.mapper.backend.question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderank.entity.pojo.QuestionTestData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目评测数据访问。
 */
@Mapper
public interface QuestionTestDataMapper extends BaseMapper<QuestionTestData> {
}
