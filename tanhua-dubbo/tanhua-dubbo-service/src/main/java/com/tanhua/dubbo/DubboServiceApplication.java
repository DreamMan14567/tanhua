package com.tanhua.dubbo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author user_Chubby
 * @date 2021/5/3
 * @Description
 */
@SpringBootApplication
@MapperScan("com.tanhua.dubbo.mapper")
public class DubboServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DubboServiceApplication.class,args);
    }
}
