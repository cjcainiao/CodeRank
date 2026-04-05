package com.coderank.codebox;

import com.coderank.entity.Task;

/**
 * 代码沙箱接口
 */
public interface CodeBox {

    /**
     * 执行任务
     * @param task
     */
    void execute(Task task);

    /**
     * 测试任务
     * @param task
     */
    void test(Task task);
}
