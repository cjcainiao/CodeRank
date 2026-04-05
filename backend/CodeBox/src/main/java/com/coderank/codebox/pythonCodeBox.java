package com.coderank.codebox;

import com.coderank.config.DockerConfig;
import com.coderank.entity.QuestionSubmit;
import com.coderank.entity.TaskContext;
import com.coderank.mapper.QuestionMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.coderank.mapper.QuestionSubmitMapper;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class pythonCodeBox extends AbstractCodeBox{

    private final DockerClient dockerClient;
    // 镜像
    private String Images = "pythoncodebox:latest";
    // 测试镜像
    private String testImage = "pythontestcodebox:latest";

    public pythonCodeBox(QuestionMapper questionMapper, QuestionSubmitMapper questionSubmitMapper, DockerConfig dockerConfig, DockerClient dockerClient) {
        super(questionMapper, questionSubmitMapper);
        this.dockerClient = dockerClient;
    }

    @Override
    protected void compile(TaskContext taskContext) {

    }

    @Override
    protected void run(TaskContext taskContext) {
        // 创建挂载目录（1、任务目录 2、测试数据目录）
        Bind taskBind = new Bind(taskContext.getTaskPath(), new Volume("/workspace/task"));
        Bind dataBind = new Bind(dataDir + File.separator + taskContext.getQuestion().getQuestionId(), new Volume("/workspace/data"), AccessMode.ro);

        // 容器配置
        HostConfig hostConfig = new HostConfig()
                .withMemory(taskContext.getQuestion().getMemory() * 1024) //内存限制
                .withMemorySwap(taskContext.getQuestion().getMemory() * 1024)
                .withBinds(taskBind, dataBind)  //数据卷挂载
                .withNetworkMode("none"); //限制网络
        // 时间限制

        // 创建容器
        CreateContainerResponse response = dockerClient.createContainerCmd(Images)
                .withHostConfig(hostConfig)
                .withCmd(String.valueOf(taskContext.getQuestion().getTimeLimit() / 1000))  //超时时间
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
            submit.setId(taskContext.getTask().getId());
            submit.setStatus(3);
            questionSubmitMapper.updateById(submit);
            throw new RuntimeException("内存溢出");
        }
    }

    /**
     * 测试任务
     * @param taskContext
     */
    protected void runTest(TaskContext taskContext) {
        // 创建挂载目录（1、任务目录 2、测试数据目录）
        Bind taskBind = new Bind(taskContext.getTaskPath(), new Volume("/workspace/task"));

        // 容器配置
        HostConfig hostConfig = new HostConfig()
                .withMemory(65535 * 1024L) //内存限制
                .withMemorySwap(65535 * 1024L)
                .withBinds(taskBind)  //数据卷挂载
                .withNetworkMode("none"); //限制网络

        // 创建容器
        CreateContainerResponse response = dockerClient.createContainerCmd(testImage)
                .withHostConfig(hostConfig)
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
    }
}
