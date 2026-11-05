package com.coderank.mapper.backend.question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderank.entity.pojo.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台标签数据访问。
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
