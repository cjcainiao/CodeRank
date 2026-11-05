package com.coderank.entity.vo.question;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 首页标签筛选项。
 */
@Data
public class TagOptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 标签名 */
    private String name;

    /** 标签颜色 */
    private String color;
}
