package com.coderank.common.enums;

import lombok.Getter;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 提交代码使用的语言。
 */
@Getter
public enum LanguageEnum {

    /** Java，编译出 Main.jar 以保留内部类 */
    JAVA("java", "Main.java",
            List.of("/bin/sh", "-c", "javac -encoding UTF-8 -d . Main.java && jar cfe Main.jar Main *.class"),
            "Main.jar",
            List.of("/usr/bin/java", "-jar", "Main.jar")),

    /** C++，编译出可执行文件 main */
    CPP("cpp", "main.cpp",
            List.of("/usr/bin/g++", "-O2", "-std=c++17", "-o", "main", "main.cpp"),
            "main",
            List.of("./main")),

    /** Python，解释执行，无编译步骤 */
    PYTHON("python", "main.py",
            null,
            null,
            List.of("/usr/bin/python3", "main.py")),

    /** Go，编译出可执行文件 main */
    GO("go", "main.go",
            List.of("/bin/sh", "-c", "go build -o main main.go"),
            "main",
            List.of("./main")),

    /** JavaScript，解释执行，无编译步骤 */
    JAVASCRIPT("javascript", "main.js",
            null,
            null,
            List.of("/usr/bin/node", "main.js"));

    /** 语言标识，前端传值 */
    private final String code;

    /** 源文件名，写入评测机的 copyIn */
    private final String sourceName;

    /** 编译命令，解释型语言为空 */
    private final List<String> compileCommand;

    /** 编译产物文件名，解释型语言为空 */
    private final String artifactName;

    /** 运行命令 */
    private final List<String> runCommand;

    LanguageEnum(String code, String sourceName, List<String> compileCommand,
                 String artifactName, List<String> runCommand) {
        this.code = code;
        this.sourceName = sourceName;
        this.compileCommand = compileCommand;
        this.artifactName = artifactName;
        this.runCommand = runCommand;
    }

    // 是否需要先编译
    public boolean needsCompile() {
        return compileCommand != null && !compileCommand.isEmpty();
    }

    // 按标识解析语言，忽略大小写、首尾空格与常见别名
    public static Optional<LanguageEnum> fromCode(String code) {
        String value = code == null ? "" : code.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "java" -> Optional.of(JAVA);
            case "cpp", "c++", "cplusplus" -> Optional.of(CPP);
            case "python", "python3", "py" -> Optional.of(PYTHON);
            case "go", "golang" -> Optional.of(GO);
            case "javascript", "js", "node" -> Optional.of(JAVASCRIPT);
            default -> Optional.empty();
        };
    }
}
