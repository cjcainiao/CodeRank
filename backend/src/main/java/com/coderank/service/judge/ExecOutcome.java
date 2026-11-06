package com.coderank.service.judge;

import com.coderank.entity.vo.judge.TestSubmitResultVO;
import lombok.Builder;

import java.util.List;

/**
 * 多测试点执行结果。
 */
@Builder
public record ExecOutcome(

        /** 编译阶段结果，解释型语言或未编译时为空 */
        TestSubmitResultVO compileResult,

        /** 各测试点的运行结果，按输入顺序排列 */
        List<TestSubmitResultVO> results
) {
}
