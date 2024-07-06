package com.example.studypartner;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wuxie
 */
@SpringBootApplication()
@MapperScan("com.example.studypartner.mapper")
@EnableScheduling
public class StudyPartnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyPartnerApplication.class, args);
    }

}
