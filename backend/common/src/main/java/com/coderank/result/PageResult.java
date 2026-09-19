package com.coderank.result;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页返回结构。
 */
@Data
@Builder
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long pageNum;

    /** 每页条数 */
    private long pageSize;

    /** 当前页数据 */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(List<T> records, long total) {
        this.records = records;
        this.total = total;
    }

    public PageResult(long total, long pageNum, long pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = records;
    }
}
