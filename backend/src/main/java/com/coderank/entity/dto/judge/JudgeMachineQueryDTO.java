package com.coderank.entity.dto.judge;

import lombok.Data;

/**
 * 评测机分页查询参数。
 */
@Data
public class JudgeMachineQueryDTO {

    /** 页码，从 1 开始 */
    private Long pageNum = 1L;

    /** 每页条数 */
    private Long pageSize = 10L;

    /** 机器名称，模糊匹配 */
    private String name;

    /** 状态：0 停用，1 启用 */
    private Integer status;

    /** 排序字段：id、weight、createTime、updateTime，为空按 id 排序 */
    private String sortField;

    /** 排序方向：asc 升序，desc 降序，为空按降序排序 */
    private String sortOrder;
}
