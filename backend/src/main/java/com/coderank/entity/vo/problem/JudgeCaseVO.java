package com.coderank.entity.vo.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 单个测试点评测结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeCaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 测试点编号，从 1 开始 */
    private Integer caseNo;

    /** 是否通过 */
    private Boolean passed;

    /** 判题状态，如 Accepted、Wrong Answer */
    private String status;

    /** 退出码 */
    private Integer exitStatus;

    /** 运行耗时，毫秒 */
    private Integer time;

    /** 运行内存，千字节 */
    private Long memory;

    /** 程序输出，仅展示型测试点返回 */
    private String output;

    /** 错误信息，通过时为空 */
    private String message;
}
