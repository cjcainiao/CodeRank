package com.coderank.controller.backend.judge;

import com.coderank.common.result.PageResult;
import com.coderank.common.result.Result;
import com.coderank.entity.dto.judge.JudgeMachineCreateDTO;
import com.coderank.entity.dto.judge.JudgeMachineQueryDTO;
import com.coderank.entity.dto.judge.JudgeMachineUpdateDTO;
import com.coderank.entity.vo.judge.JudgeMachineCheckVO;
import com.coderank.entity.vo.judge.JudgeMachineVO;
import com.coderank.entity.vo.judge.JudgeMetricsVO;
import com.coderank.service.backend.judge.JudgeMachineService;
import com.coderank.service.backend.judge.JudgeMetricsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台评测机管理接口。
 */
@RestController
@RequestMapping("/backend/judge-machines")
public class JudgeMachineController {

    private final JudgeMachineService judgeMachineService;

    private final JudgeMetricsService judgeMetricsService;

    public JudgeMachineController(JudgeMachineService judgeMachineService,
                                  JudgeMetricsService judgeMetricsService) {
        this.judgeMachineService = judgeMachineService;
        this.judgeMetricsService = judgeMetricsService;
    }

    // 分页查询评测机
    @GetMapping
    public Result<PageResult<JudgeMachineVO>> pageMachines(JudgeMachineQueryDTO queryDTO) {
        return Result.success(judgeMachineService.pageMachines(queryDTO));
    }

    // 按 id 查询评测机
    @GetMapping("/{id}")
    public Result<JudgeMachineVO> getMachineById(@PathVariable Long id) {
        return Result.success(judgeMachineService.getMachineById(id));
    }

    // 新增评测机，主键转字符串避免前端精度丢失
    @PostMapping
    public Result<String> createMachine(@RequestBody @Valid JudgeMachineCreateDTO createDTO) {
        return Result.success(String.valueOf(judgeMachineService.createMachine(createDTO)));
    }

    // 修改评测机
    @PutMapping
    public Result<Void> updateMachine(@RequestBody @Valid JudgeMachineUpdateDTO updateDTO) {
        judgeMachineService.updateMachine(updateDTO);
        return Result.success();
    }

    // 删除评测机
    @DeleteMapping("/{id}")
    public Result<Void> deleteMachine(@PathVariable Long id) {
        judgeMachineService.deleteMachine(id);
        return Result.success();
    }

    // 探测评测机是否可用
    @PostMapping("/{id}/check")
    public Result<JudgeMachineCheckVO> checkMachine(@PathVariable Long id) {
        return Result.success(judgeMachineService.checkMachine(id));
    }

    // 探测全部评测机
    @PostMapping("/check-all")
    public Result<List<JudgeMachineCheckVO>> checkAllMachines() {
        return Result.success(judgeMachineService.checkAllMachines());
    }

    // 抓取评测机运行指标
    @GetMapping("/{id}/metrics")
    public Result<JudgeMetricsVO> getMetrics(@PathVariable Long id) {
        return Result.success(judgeMetricsService.getMetrics(id));
    }
}
