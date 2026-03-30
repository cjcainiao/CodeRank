package com.coderank.codeBox;

import com.coderank.entity.Question;
import com.coderank.entity.QuestionSubmit;
import com.coderank.entity.Task;
import com.coderank.mapper.QuestionMapper;
import com.coderank.mapper.QuestionSubmitMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * python代码沙箱
 */
@Component("python")
@RequiredArgsConstructor
public class pythonCodebox extends AbstractCodeBox {

    private final DockerClient dockerClient;
    private final QuestionMapper questionMapper;
    private final QuestionSubmitMapper questionSubmitMapper;

    // 镜像
    private String Images = "pythoncodebox:latest";


    @Override
    protected void run(String taskPath, Task task) {

        Question question = questionMapper.selectById(task.getQuestionId());

        // 创建挂载目录（1、任务目录 2、测试数据目录）
        Bind taskBind = new Bind(taskPath, new Volume("/workspace/task"));
        Bind dataBind = new Bind(dataDir + File.separator + task.getQuestionId(), new Volume("/workspace/data"), AccessMode.ro);

        // 容器配置
        HostConfig hostConfig = new HostConfig()
                .withMemory(question.getMemory() * 1024) //内存限制
                .withMemorySwap(question.getMemory() * 1024)
                .withBinds(taskBind, dataBind)  //数据卷挂载
                .withNetworkMode("none"); //限制网络
        // 时间限制

        // 创建容器
        CreateContainerResponse response = dockerClient.createContainerCmd(Images)
                .withHostConfig(hostConfig)
                .withCmd(String.valueOf(question.getTimeLimit() / 1000))  //超时时间
                .exec();

        //启动容器
        dockerClient.startContainerCmd(response.getId()).exec();

        //等待容器执行完成删除容器
        WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
        int exitCode = dockerClient.waitContainerCmd(response.getId())
                .exec(waitCallback)
                .awaitStatusCode();

        System.out.println("容器退出码: " + exitCode);

        dockerClient.removeContainerCmd(response.getId())
                .withForce(true)
                .exec();

        // 判断程序内存是否溢出
        if (exitCode == 137 || exitCode == 9) {
            QuestionSubmit submit = new QuestionSubmit();
            submit.setId(task.getId());
            submit.setStatus(4);
            questionSubmitMapper.updateById(submit);
            throw new RuntimeException("内存溢出");
        }
    }

    @Override
    protected void compile(String task) {

    }
}
