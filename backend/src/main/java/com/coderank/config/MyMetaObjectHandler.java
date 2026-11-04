package com.coderank.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公共字段自动填充。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /** 创建时间字段名 */
    private static final String FIELD_CREATE_TIME = "createTime";

    /** 更新时间字段名 */
    private static final String FIELD_UPDATE_TIME = "updateTime";

    // 插入时填充创建时间与更新时间
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, FIELD_CREATE_TIME, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, FIELD_UPDATE_TIME, LocalDateTime.class, now);
    }

    // 更新时刷新更新时间
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, FIELD_UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }
}
