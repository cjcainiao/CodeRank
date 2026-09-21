package com.coderank.judge;

/**
 * 任务评测接口
 */
public interface Judge {

    // 1、查询评测任务
    void query(JudgeContext context);

    // 2、检测危险命令
    void check(JudgeContext context);

    // 3、组装代码
    void assembly(JudgeContext context);

    // 4、编译代码
    void compile(JudgeContext context);

    // 5、运行代码
    void run(JudgeContext context);

    // 运行测试代码
    void runTest(JudgeContext context);

    // 运行输出结果脚本
    void runOutputScript(JudgeContext context);

    // 6、获取运行结果并判题
    void judge(JudgeContext context);

    // 7、清理任务资源
    void cleanup(JudgeContext context);

    /**
     * 执行评测任务。
     */
    void executeTask(Long submissionId);

    /**
     * 执行测试任务
     */
    void executeTestTask(Long submissionId);
}
