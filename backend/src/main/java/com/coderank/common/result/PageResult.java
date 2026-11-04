package com.coderank.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 统一分页返回体。
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页码，从 1 开始 */
    private long pageNum;

    /** 每页条数 */
    private long pageSize;

    /** 总页数 */
    private long pages;

    private PageResult(List<T> records, long total, long pageNum, long pageSize, long pages) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pages;
    }

    // 空分页，用于无数据场景
    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return new PageResult<>(Collections.emptyList(), 0L, pageNum, pageSize, 0L);
    }

    // 由记录、总数和分页参数构造
    public static <T> PageResult<T> of(List<T> records, long total, long pageNum, long pageSize) {
        long pages = pageSize <= 0 ? 0L : (total + pageSize - 1) / pageSize;
        return new PageResult<>(records, total, pageNum, pageSize, pages);
    }

    // 由 MyBatis-Plus 的 IPage 转换
    public static <T> PageResult<T> of(IPage<T> page) {
        return of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    // 由 MyBatis-Plus 的 IPage 转换并映射元素类型
    public static <S, T> PageResult<T> of(IPage<S> page, Function<S, T> mapper) {
        List<T> list = page.getRecords().stream().map(mapper).toList();
        return of(list, page.getTotal(), page.getCurrent(), page.getSize());
    }
}
