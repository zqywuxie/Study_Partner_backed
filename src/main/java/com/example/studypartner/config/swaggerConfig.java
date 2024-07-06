package com.example.studypartner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.ArrayList;

/**
 * 2022/10/23
 *
 * @version 1.0
 * @Author:zqy
 */
@Configuration
@EnableSwagger2WebMvc
public class swaggerConfig {
    /**
     配置swagger实例
     *
     */
    @Bean
    public Docket docket(Environment environment) {

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.studypartner.controller"))
                .build();
    }

    /**
     配置Swagger信息
     *
      */
    private ApiInfo apiInfo() {
        Contact contact = new Contact("zqy", "https://zqywuxie.top", "573905257@qq.com");
        return new ApiInfo(
                // 标题
                "后端接口",
                // 描述
                "前后端分离项目后端接口",
                // 版本
                "v1.0",
                // 组织链接
                "http://terms.service.url/组织链接",
                // 联系人信息
                contact,
                // 许可
                "Apach 2.0 许可",
                // 许可连接
                "许可链接",
                // 扩展
                new ArrayList<>()
        );
    }
}
