package com.xwz.xwzpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.xwz.xwzpicturebackend.mapper")
public class XwzPictureBackendApplication {


    public static void main(String[] args) {
        SpringApplication.run(XwzPictureBackendApplication.class, args);
    }

}


