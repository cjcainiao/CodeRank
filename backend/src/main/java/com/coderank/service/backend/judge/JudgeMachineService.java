package com.coderank.service.backend.judge;

import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.judge.JudgeMachineCreateDTO;
import com.coderank.entity.dto.judge.JudgeMachineQueryDTO;
import com.coderank.entity.dto.judge.JudgeMachineUpdateDTO;
import com.coderank.entity.vo.judge.JudgeMachineCheckVO;
import com.coderank.entity.vo.judge.JudgeMachineVO;

import java.util.List;

/**
 * 后台评测机管理业务。
 */
public interface JudgeMachineService {

    // 分页查询评测机
    PageResult<JudgeMachineVO> pageMachines(JudgeMachineQueryDTO queryDTO);

    // 按 id 查询评测机
    JudgeMachineVO getMachineById(Long id);

    // 新增评测机
    Long createMachine(JudgeMachineCreateDTO createDTO);

    // 修改评测机
    void updateMachine(JudgeMachineUpdateDTO updateDTO);

    // 删除评测机
    void deleteMachine(Long id);

    // 探测评测机是否可用，并回写探测结果
    JudgeMachineCheckVO checkMachine(Long id);

    // 探测全部评测机，返回每台的探测结果
    List<JudgeMachineCheckVO> checkAllMachines();
}
