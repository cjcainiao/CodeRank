package com.coderank.mapper.backend.question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderank.entity.pojo.QuestionCodeTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目代码模板数据访问。
 */
@Mapper
public interface QuestionCodeTemplateMapper extends BaseMapper<QuestionCodeTemplate> {
}
