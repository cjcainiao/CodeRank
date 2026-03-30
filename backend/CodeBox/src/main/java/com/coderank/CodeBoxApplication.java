package com.coderank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.coderank.mapper")
public class CodeBoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeBoxApplication.class, args);
    }

}
