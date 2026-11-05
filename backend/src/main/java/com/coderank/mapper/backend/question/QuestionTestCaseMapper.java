package com.coderank.mapper.backend.question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderank.entity.pojo.QuestionTestCase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目测试用例数据访问。
 */
@Mapper
public interface QuestionTestCaseMapper extends BaseMapper<QuestionTestCase> {
}
