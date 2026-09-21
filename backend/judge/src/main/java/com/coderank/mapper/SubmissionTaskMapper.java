package com.coderank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderank.entity.SubmissionTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提交任务数据访问层。
 */
@Mapper
public interface SubmissionTaskMapper extends BaseMapper<SubmissionTask> {
}
