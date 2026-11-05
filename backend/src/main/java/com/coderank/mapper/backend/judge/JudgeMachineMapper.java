package com.coderank.mapper.backend.judge;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderank.entity.pojo.JudgeMachine;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台评测机数据访问。
 */
@Mapper
public interface JudgeMachineMapper extends BaseMapper<JudgeMachine> {
}
