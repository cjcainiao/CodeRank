package com.coderank.result;

import lombok.Data;

/**
 * 通用分页请求参数。
 */
@Data
public class PageQuery {

    /** 当前页码，默认 1。 */
    private long pageNum = 1;

    /** 每页条数，默认 10，最大 100。 */
    private long pageSize = 10;

    /** 排序字段。 */
    private String sortField;

    /** 排序方式：asc 升序，desc 降序。 */
    private String sortOrder;

    public void setPageNum(long pageNum) {
        this.pageNum = Math.max(pageNum, 1);
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 100);
    }

    /** 查询起始行号，供手写 SQL 的 limit 使用（Page 分页无需）。 */
    public long getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
