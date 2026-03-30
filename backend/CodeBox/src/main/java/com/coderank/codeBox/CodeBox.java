package com.coderank.codeBox;

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
}
