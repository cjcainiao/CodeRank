package com.coderank.entity.vo.question;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 首页题目详情。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 题目标题 */
    private String title;

    /** 难度：1 简单，2 中等，3 困难 */
    private Integer difficulty;

    /** 判题模式：1 标准输入输出，2 核心代码 */
    private Integer judgeMode;

    /** 题目描述，Markdown */
    private String description;

    /** 输入格式说明，Markdown */
    private String inputFormat;

    /** 输出格式说明，Markdown */
    private String outputFormat;

    /** 样例，[{"input":"","output":""}] */
    private String sampleCases;

    /** 提示，Markdown */
    private String hint;

    /** 时间限制，毫秒 */
    private Integer timeLimit;

    /** 内存限制，MB */
    private Integer memoryLimit;

    /** 题目来源 */
    private String source;

    /** 提交次数 */
    private Integer submitCount;

    /** 通过次数 */
    private Integer acceptedCount;

    /** 标签名列表 */
    private List<String> tags;

    /** 各语言初始代码 */
    private List<CodeTemplate> codeTemplates;

    /** 测试运行可快捷填入的用例 */
    private List<TestCaseSample> testCases;

    /**
     * 语言初始代码。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeTemplate implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 语言：1 Java，2 C++，3 Python，4 Go，5 JavaScript */
        private Integer language;

        /** 初始代码 */
        private String template;
    }

    /**
     * 测试运行用的快捷用例。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseSample implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 用例编号，从 1 开始 */
        private Integer caseNo;

        /** 标准输入，核心代码模式为 args JSON */
        private String input;

        /** 期望输出，仅展示型用例返回 */
        private String expected;
    }
}
