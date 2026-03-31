package com.coderank.client;


import cn.hutool.json.JSONUtil;
import com.coderank.codeBox.AbstractCodeBox;
import com.coderank.codeBox.CodeboxManager;
import com.coderank.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 任务接收类（kafka）
 */
@Component
@RequiredArgsConstructor
public class Consumer {

    private final CodeboxManager codeboxManager;

    @KafkaListener(topics = "${task.topic}", groupId = "${task.groupId}")
    public void consumer(String taskRequest, Acknowledgment ack) {
        try {
            // 转换对象
            Task task = JSONUtil.toBean(taskRequest, Task.class);

            // 通过语言获取具体的代码沙箱
            AbstractCodeBox codeBox = codeboxManager.getByLanguage(task.getLanguage());

            // 执行任务
            codeBox.execute(task);

            // 手动提交偏移量
            ack.acknowledge();  // 成功处理后提交
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
