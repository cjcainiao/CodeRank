package com.coderank.entity.vo.judge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 测试提交运行结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSubmitResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评测机主键 */
    private Long machineId;

    /** 评测机名称 */
    private String machineName;

    /** 出错步骤：compile 编译，run 运行 */
    private String stage;

    /** 判题状态，如 Accepted、Nonzero Exit Status */
    private String status;

    /** 退出码 */
    private Integer exitStatus;

    /** 运行耗时，纳秒 */
    private Long time;

    /** 运行内存，字节 */
    private Long memory;

    /** 标准输出 */
    private String stdout;

    /** 标准错误 */
    private String stderr;

    /** 评测机返回的错误信息，正常时为空 */
    private String error;
}
