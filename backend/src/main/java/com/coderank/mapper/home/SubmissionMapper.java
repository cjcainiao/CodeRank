package com.coderank.mapper.home;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderank.entity.pojo.Submission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户端提交记录数据访问。
 */
@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {
}
