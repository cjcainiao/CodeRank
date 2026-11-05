package com.coderank.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增标签请求参数。
 */
@Data
public class TagCreateDTO {

    /** 标签名，如二分、动态规划 */
    @NotBlank(message = "标签名不能为空")
    @Size(max = 50, message = "标签名长度不能超过 50")
    private String name;

    /** 标签颜色，十六进制色值 */
    @Pattern(regexp = "^$|^#[0-9a-fA-F]{6}$", message = "标签颜色须为 #RRGGBB 格式")
    private String color;

    /** 排序值，越小越靠前 */
    private Integer sortOrder;
}
