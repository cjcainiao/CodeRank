package com.coderank.service.backend.judge;

import com.coderank.entity.dto.judge.TestSubmitDTO;
import com.coderank.entity.vo.judge.TestSubmitResultVO;

/**
 * 测试提交业务。
 */
public interface JudgeTestSubmitService {

    // 把代码提交到指定评测机运行一次
    TestSubmitResultVO submit(TestSubmitDTO submitDTO);
}
