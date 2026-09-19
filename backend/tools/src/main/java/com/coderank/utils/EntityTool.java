package com.coderank.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.model.ClassAnnotationAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

/**
 * MyBatis-Plus 实体类生成工具。
 *
 * <p>直接运行 main 方法，根据环境变量连接数据库并生成全部数据表的实体类：</p>
 * <ul>
 *     <li>DB_URL：JDBC 连接地址</li>
 *     <li>DB_USERNAME：数据库用户名</li>
 *     <li>DB_PASSWORD：数据库密码</li>
 *     <li>DATA_PATH：Java 源码输出目录</li>
 * </ul>
 */
public final class EntityTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityTool.class);

    private EntityTool() {
    }

    public static void main(String[] args) {
        String jdbcUrl = requiredEnvironment("DB_URL");
        String username = requiredEnvironment("DB_USERNAME");
        String password = requiredEnvironment("DB_PASSWORD");
        String outputDirectory = requiredEnvironment("DATA_PATH");

        generateAll(
                jdbcUrl,
                username,
                password,
                outputDirectory
        );
    }

    /**
     * 根据数据库中的全部数据表生成实体类。
     */
    public static void generateAll(
            String jdbcUrl,
            String username,
            String password,
            String outputDirectory
    ) {
        requireText(jdbcUrl, "数据库连接地址不能为空");
        requireText(username, "数据库用户名不能为空");
        requireText(outputDirectory, "实体类输出目录不能为空");

        Path entityOutputPath = Path.of(outputDirectory).normalize();
        if (!entityOutputPath.isAbsolute()) {
            throw new IllegalArgumentException("DATA_PATH 必须是绝对路径");
        }
        String entityPackage = resolvePackageName(entityOutputPath);
        LOGGER.info("开始生成实体类，目标包：{}，输出目录：{}", entityPackage, entityOutputPath);

        // 生成前先删除目标目录下已存在的旧实体类，避免残留文件
        deleteExistingEntities(entityOutputPath);

        FastAutoGenerator.create(jdbcUrl, username, password)
                .globalConfig(builder -> builder
                        .author("cainiao")
                        .outputDir(entityOutputPath.toString())
                        .disableOpenDir())
                .packageConfig(builder -> builder
                        .parent(entityPackage)
                        .entity("")
                        .pathInfo(Map.of(OutputFile.entity, entityOutputPath.toString())))
                .strategyConfig(builder -> {
                    builder.enableSkipView();
                    builder.entityBuilder()
                            .enableLombok(
                                    new ClassAnnotationAttributes("@Data", "lombok.Data"),
                                    new ClassAnnotationAttributes("@Builder", "lombok.Builder"))
                            .enableFileOverride();

                    builder.controllerBuilder().disable();
                    builder.serviceBuilder().disable();
                    builder.mapperBuilder().disable();
                })
                .templateConfig(builder -> builder
                        // 使用自定义实体模板：强制为所有实体类输出 @TableName 注解
                        .entity("/templates/entity.java.ftl"))
                .templateEngine(new EntityFreemarkerTemplateEngine())
                .execute();

        LOGGER.info("实体类生成完成");
    }

    /**
     * 删除目标目录下已存在的旧实体类文件（仅 *.java），确保重新生成时无残留。
     */
    private static void deleteExistingEntities(Path outputDirectory) {
        if (outputDirectory == null || !Files.isDirectory(outputDirectory)) {
            return;
        }
        try (Stream<Path> paths = Files.list(outputDirectory)) {
            paths.filter(p -> p.getFileName().toString().endsWith(".java"))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                            LOGGER.info("已删除旧实体类：{}", p);
                        } catch (IOException e) {
                            throw new UncheckedIOException("删除旧实体类失败：" + p, e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException("读取实体类输出目录失败：" + outputDirectory, e);
        }
    }

    /**
     * 使用默认实体类模板，并移除模板中固定生成的 @since 注释。
     */
    private static final class EntityFreemarkerTemplateEngine extends FreemarkerTemplateEngine {

        private static final String SINCE_COMMENT_LINE =
                "(?m)^[\\t ]*\\*[\\t ]+@since[^\\r\\n]*(?:\\r\\n|\\n|\\r)?";

        @Override
        public void writer(Map<String, Object> objectMap, String templatePath, File outputFile)
                throws Exception {
            super.writer(objectMap, templatePath, outputFile);

            if (!templatePath.replace('\\', '/').contains("/entity.java")) {
                return;
            }

            Path entityFile = outputFile.toPath();
            String content = Files.readString(entityFile, StandardCharsets.UTF_8);
            String updatedContent = content.replaceFirst(SINCE_COMMENT_LINE, "");
            if (!content.equals(updatedContent)) {
                Files.writeString(entityFile, updatedContent, StandardCharsets.UTF_8);
            }
            LOGGER.info("已生成实体类：{}", entityFile);
        }
    }

    private static String requiredEnvironment(String environmentName) {
        String value = System.getenv(environmentName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("缺少环境变量：" + environmentName);
        }
        return value.trim();
    }

    private static String resolvePackageName(Path outputDirectory) {
        int packageStart = -1;
        for (int index = 0; index <= outputDirectory.getNameCount() - 3; index++) {
            if ("src".equals(outputDirectory.getName(index).toString())
                    && "main".equals(outputDirectory.getName(index + 1).toString())
                    && "java".equals(outputDirectory.getName(index + 2).toString())) {
                packageStart = index + 3;
            }
        }

        if (packageStart < 0 || packageStart >= outputDirectory.getNameCount()) {
            throw new IllegalArgumentException(
                    "DATA_PATH 必须指向 src/main/java 下的具体包目录"
            );
        }

        StringBuilder packageName = new StringBuilder();
        for (int index = packageStart; index < outputDirectory.getNameCount(); index++) {
            if (!packageName.isEmpty()) {
                packageName.append('.');
            }
            packageName.append(outputDirectory.getName(index));
        }
        return packageName.toString();
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
