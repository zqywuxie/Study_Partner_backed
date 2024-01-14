package com.example.studypartner.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redission配置
 * @author wuxie
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
	private String host;
	private String port;
	private String password;

	@Bean
	public RedissonClient redissonClient() {
		// 1. Create config object 配置
		Config config = new Config();
		String redisAddress = String.format("redis://%s:%s", host, port);
		config.useSingleServer()
				.setAddress(redisAddress)
				.setDatabase(1)
				.setPassword(password);
		// 2. Create Redisson instance 实例
		return Redisson.create(config);
	}
}
