package com.coderank.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.coderank.entity.SubmissionTask;
import com.coderank.mapper.SubmissionTaskMapper;
import com.coderank.service.TestRunService;
import org.springframework.stereotype.Service;

/**
 * 测试运行服务实现。
 */
@Service
public class TestRunServiceImpl extends ServiceImpl<SubmissionTaskMapper, SubmissionTask> implements TestRunService {

    // 提交任务
    public void submit(){

        // 1、根据语言获取不同构造的请求
    }
}
