package com.example.studypartner;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wuxie
 */
@SpringBootApplication
@MapperScan("com.example.studypartner.mapper")
public class StudyPartnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyPartnerApplication.class, args);
    }

}
