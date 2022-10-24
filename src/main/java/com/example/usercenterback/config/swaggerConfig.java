package com.example.usercenterback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

/**
 * 2022/10/23
 *
 * @version 1.0
 * @Author:zqy
 */
@Configuration
@EnableSwagger2
public class swaggerConfig {
    // 配置swagger实例
    @Bean
    public Docket docket(Environment environment) {
        // Profiles profiles=Profiles.of();
        // boolean b = environment.acceptsProfiles(profiles);
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.usercenterback.controller"))
                .build();
    }

    // 配置Swagger信息
    private ApiInfo apiInfo() {
        Contact contact = new Contact("zqy", "https://zqywuxie.top", "573905257@qq.com");
        return new ApiInfo(
                "后端接口", // 标题
                "前后端分离项目后端接口", // 描述
                "v1.0", // 版本
                "http://terms.service.url/组织链接", // 组织链接
                contact, // 联系人信息
                "Apach 2.0 许可", // 许可
                "许可链接", // 许可连接
                new ArrayList<>()// 扩展
        );
    }
}
