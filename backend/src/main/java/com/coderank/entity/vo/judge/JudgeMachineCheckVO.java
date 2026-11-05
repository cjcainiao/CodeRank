package com.coderank.entity.vo.judge;

import lombok.Data;

import java.io.Serializable;

/**
 * 评测机探测结果。
 */
@Data
public class JudgeMachineCheckVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否连通 */
    private Boolean ok;

    /** 探测耗时，毫秒 */
    private Long elapsed;

    /** 评测机版本，探测失败时为空 */
    private String version;

    /** 操作系统 */
    private String os;

    /** 处理器架构 */
    private String platform;

    /** 失败原因，探测成功时为空 */
    private String message;
}
