package com.coderank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderank.entity.QuestionAnswerFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目答案文件数据访问层。
 */
@Mapper
public interface QuestionAnswerFileMapper extends BaseMapper<QuestionAnswerFile> {
}
