package com.coderank.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件上传配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {

    /** 上传根目录 */
    private String root;

    /** 题目评测数据配置 */
    private TestData testData;

    /**
     * 题目评测数据配置。
     */
    @Data
    public static class TestData {

        /** 评测数据子目录 */
        private String dir;

        /** 允许的扩展名 */
        private List<String> allowedExtensions;
    }
}
