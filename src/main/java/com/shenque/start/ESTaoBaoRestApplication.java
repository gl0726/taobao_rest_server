package com.shenque.start;


import org.apache.log4j.Logger;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * xiao.py
 * //(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})  //如果暂时不需要数据源，可将pom文件中的mysql和mybatis（或其他数据源框架）注释掉
 */

@EnableScheduling
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Configuration
@ComponentScan(basePackages = "com.shenque")
@EnableSwagger2
public class ESTaoBaoRestApplication {
    private static final Logger logger = Logger.getLogger(ESTaoBaoRestApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(ESTaoBaoRestApplication.class, args);
    }


}
