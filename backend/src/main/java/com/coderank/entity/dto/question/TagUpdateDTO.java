package com.coderank.entity.dto.question;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改标签请求参数。
 */
@Data
public class TagUpdateDTO {

    /** 主键 */
    @NotNull(message = "标签 id 不能为空")
    private Long id;

    /** 标签名，如二分、动态规划 */
    @Size(max = 50, message = "标签名长度不能超过 50")
    private String name;

    /** 标签颜色，十六进制色值 */
    @Pattern(regexp = "^$|^#[0-9a-fA-F]{6}$", message = "标签颜色须为 #RRGGBB 格式")
    private String color;

    /** 排序值，越小越靠前 */
    private Integer sortOrder;
}
