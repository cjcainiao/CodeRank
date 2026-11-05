package com.coderank.entity.dto.user;

import lombok.Data;

/**
 * 用户分页查询参数。
 */
@Data
public class UserQueryDTO {

    /** 页码，从 1 开始 */
    private Long pageNum = 1L;

    /** 每页条数 */
    private Long pageSize = 10L;

    /** 登录账号，模糊匹配 */
    private String username;

    /** 昵称，模糊匹配 */
    private String nickname;

    /** 账号状态：0 正常，1 禁用 */
    private Integer status;

    /** 角色：0 普通用户，1 管理员 */
    private Integer role;

    /** 排序字段：id、createTime、updateTime，为空按 id 排序 */
    private String sortField;

    /** 排序方向：asc 升序，desc 降序，为空按降序排序 */
    private String sortOrder;
}
