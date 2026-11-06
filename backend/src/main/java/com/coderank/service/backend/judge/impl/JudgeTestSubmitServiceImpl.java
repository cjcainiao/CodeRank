package com.coderank.service.backend.judge.impl;

import com.coderank.common.enums.ResultCodeEnum;
import com.coderank.common.enums.LanguageEnum;
import com.coderank.entity.dto.judge.TestSubmitDTO;
import com.coderank.entity.pojo.JudgeMachine;
import com.coderank.entity.vo.judge.TestSubmitResultVO;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.backend.judge.JudgeMachineMapper;
import com.coderank.service.backend.judge.JudgeTestSubmitService;
import com.coderank.service.judge.JudgeExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 后台测试提交业务实现，按指定评测机执行一次代码。
 */
@Service
public class JudgeTestSubmitServiceImpl implements JudgeTestSubmitService {

    /** 机器状态：启用 */
    private static final int STATUS_ENABLED = 1;

    private final JudgeMachineMapper judgeMachineMapper;

    private final JudgeExecutor judgeExecutor;

    public JudgeTestSubmitServiceImpl(JudgeMachineMapper judgeMachineMapper, JudgeExecutor judgeExecutor) {
        this.judgeMachineMapper = judgeMachineMapper;
        this.judgeExecutor = judgeExecutor;
    }

    // 提交代码到指定评测机，需要编译的语言先编译再运行
    @Override
    public TestSubmitResultVO submit(TestSubmitDTO submitDTO) {
        JudgeMachine machine = judgeMachineMapper.selectById(submitDTO.getMachineId());
        if (machine == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "评测机不存在");
        }
        if (!Integer.valueOf(STATUS_ENABLED).equals(machine.getStatus())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "评测机已停用");
        }
        if (!StringUtils.hasText(machine.getEndpoint())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "评测机未配置服务地址");
        }

        LanguageEnum language = LanguageEnum.fromCode(submitDTO.getLanguage())
                .orElseThrow(() -> new BusinessException(ResultCodeEnum.BAD_REQUEST, "不支持的语言"));

        // 后台测试不绑定题目，沿用机器自身限额
        return judgeExecutor.execute(machine, language, submitDTO.getCode(), submitDTO.getInput(), null);
    }
}
