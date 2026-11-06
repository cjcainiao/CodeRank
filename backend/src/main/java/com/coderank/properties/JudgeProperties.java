package com.coderank.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 评测配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "judge")
public class JudgeProperties {

    /** 编译阶段配置 */
    private Compile compile = new Compile();

    /** 运行阶段默认配置 */
    private Run run = new Run();

    /** 沙箱环境变量 */
    private Sandbox sandbox = new Sandbox();

    /**
     * 编译阶段限额，编译器比用户程序更吃资源。
     */
    @Data
    public static class Compile {

        /** CPU 时间上限 */
        private Duration cpuLimit = Duration.ofSeconds(20);

        /** 墙钟时间上限 */
        private Duration clockLimit = Duration.ofSeconds(30);

        /** 内存上限，字节 */
        private Long memoryLimit = 1_073_741_824L;

        /** 进程数上限 */
        private Long procLimit = 256L;
    }

    /**
     * 运行阶段兜底限额，题目未配置时使用。
     */
    @Data
    public static class Run {

        /** CPU 时间上限 */
        private Duration cpuLimit = Duration.ofSeconds(5);

        /** 墙钟时间上限 */
        private Duration clockLimit = Duration.ofSeconds(10);

        /** 内存上限，字节 */
        private Long memoryLimit = 268_435_456L;

        /** 进程数上限 */
        private Long procLimit = 64L;

        /** 输出上限，字节 */
        private Long outputLimit = 1_048_576L;

        /** 墙钟时间相对 CPU 时间的放大倍数，给解释器和启动开销留余量 */
        private Long clockFactor = 2L;
    }

    /**
     * 沙箱环境。
     */
    @Data
    public static class Sandbox {

        /** 容器内可执行文件搜索路径 */
        private String path = "/usr/local/bin:/usr/bin:/bin";
    }
}
