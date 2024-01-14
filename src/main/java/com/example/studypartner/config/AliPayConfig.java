package com.example.studypartner.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author wuxie
 * @date 2024/1/14 19:32
 * @description 该文件的描述 todo
 */

@Configuration
@Data
@PropertySource("classpath:alipay.properties")
public class AliPayConfig {

	@Value("${ali.privateKey}")
	private String privateKey;

	@Value("${ali.alipayPublicKey}")
	private String alipayPublicKey;

	@Value("${ali.appId}")
	private String appId;

	@Value("${ali.serverUrl}")
	private String serverUrl;

	@Value("${ali.notifyUrl}")
	private String notifyUrl;

	@Value("${ali.returnUrl}")
	private String returnUrl;


	@Bean
	public AlipayClient alipayClient() throws AlipayApiException {
		com.alipay.api.AlipayConfig alipayConfig = new com.alipay.api.AlipayConfig();
		alipayConfig.setServerUrl(serverUrl);
		alipayConfig.setAppId(appId);
		alipayConfig.setPrivateKey(privateKey);
		alipayConfig.setFormat("json");
		alipayConfig.setAlipayPublicKey(alipayPublicKey);
		alipayConfig.setCharset("UTF8");
		alipayConfig.setSignType("RSA2");

		// 3.创建支付宝的默认的客户端
		return new DefaultAlipayClient(alipayConfig);
	}
	// 2.把需要配置的参数set进AlipayConfig类中

}
