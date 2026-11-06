package com.coderank.entity.vo.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 提交评测结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 题目主键 */
    private Long questionId;

    /** 总判题状态，如 Accepted、Wrong Answer、Compile Error */
    private String status;

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

    /** 错误信息，编译错误或运行错误时返回 */
    private String message;

    /** 各测试点结果 */
    private List<JudgeCaseVO> cases;
}
