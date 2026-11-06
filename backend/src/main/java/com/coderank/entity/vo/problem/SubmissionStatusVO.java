package com.coderank.entity.vo.problem;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 提交评测任务状态。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 提交主键，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long submissionId;

    /** 任务状态：0 等待，1~7 已出结果，8 评测中 */
    private Integer status;

    /** 是否评测完成，前端据此停止轮询 */
    private Boolean finished;

    /** 总判题状态，如 Accepted、Wrong Answer */
    private String result;

    /** 是否全部通过 */
    private Boolean accepted;

    /** 通过测试点数 */
    private Integer passedCount;

    /** 测试点总数 */
    private Integer totalCount;

    /** 最大耗时，毫秒 */
    private Integer timeUsed;

    /** 最大内存，千字节 */
    private Long memoryUsed;

    /** 错误信息 */
    private String message;

    /** 各测试点结果，评测完成后返回 */
    private List<JudgeCaseVO> cases;
}
