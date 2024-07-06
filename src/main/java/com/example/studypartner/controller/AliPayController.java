package com.example.studypartner.controller;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 支付宝的支付接口
 * 用户名：flrnht7304@sandbox.com
 * 密码：111111
 * @author wuxie
 */

// http://localhost:8081/api/ali/pay?orderId=202307302343&price=12.56&subject=iPhone
@RestController
@RequestMapping("/ali")
@Slf4j
public class AliPayController {


	@Resource
	private AlipayClient alipayClient;

	@Value("${ali.alipayPublicKey}")
	private String alipayPublicKey;
	/**
	 *  支付成功返回的页面
	 */
	@Value("${ali.returnUrl}")
	private String returnUrl;

	/**
	 支付宝支付成功异步回调的页面，需要内网穿透
 	 */
	@Value("${ali.notifyUrl}")
	private String notifyUrl;

	@GetMapping("/pay")
	public void payOrder(String orderId, String price, String subject, HttpServletResponse httpResponse) throws AlipayApiException, IOException {
		// 1.准备需要的参数，私匙，支付公匙，appid，沙箱支付平台的url


		// 4.准备支付的参数，包括需要支付的订单的id，价格，物品名称，
		AlipayTradePagePayModel model = new AlipayTradePagePayModel();
//        model.setOutTradeNo("2023073022083620");
		model.setOutTradeNo(orderId); // 需要支付的订单id，自定义的订单id，不能重复，唯一，已经支付的无法继续支付
		model.setTotalAmount(price); // 需要支付的钱 model.setTotalAmount("88.88");
		model.setSubject(subject); // 要支付的物品，比如 model.setSubject("Iphone6 16G");
		model.setProductCode("FAST_INSTANT_TRADE_PAY");

		// 5.创建支付的请求，把上面的准备支付的参数进行set
		AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
		request.setBizModel(model);

		/**
		 * 设置支付宝异步回调，这个需要用内网穿透
		 */
		request.setNotifyUrl(notifyUrl);

		/**
		 * 设置支付宝支付成功返回的页面
		 * 这里让支付成功直接回到static下的一个静态页面中
		 */
		request.setReturnUrl(returnUrl);

		// 6.调用ali客户端alipayClient，用客户端执行请求，获取响应，获取.getBody()，拿到form表单
		// 执行请求，拿到响应的结果，返回给浏览器
		String form = "";
		try {
			form = alipayClient.pageExecute(request).getBody(); // 调用SDK生成表单
			log.debug(">>>>>>getAliPayFrom");
			System.out.println("getAliPayFrom");
			System.out.println(form);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}

		// 表单数据作为响应返回给前端，显示前端页面
		/**
		 * <form name="punchout_form" method="post" action="https://openapi-sandbox.dl.alipaydev.com/gateway.do?charset=UTF8&method=alipay.trade.page.pay&sign=ZSYIysQXMlJK6q%2B%2BQVJVhKi3qtHBaXTMmh4%2Fi7Wcj1OuSr5Qs6X%2Fhq0jgDbR%2BFw6da3fO5Hov8iV8%2BPo07CVnIQDSL1X2WDZzSN3Srqid%2BLKP%2BnoME2Jv9AfhaPwYkbElg0x40pLspkCv7%2FeRs93ROkrvLUL0aTA%2B7wXrIe5FKkvptqfqjReMayVR1lKmiGw8SWToNJ0OQoDp4g6191vJIy9ld%2BQfRU6PLq9dUH3XIifNvCwOceMCqpOXNlJIAb8MC2FkXK%2FCwEij5mj%2BpqNEevMf0DRczuUklJe20FeXrx6bSsjEIly8Swt1j4UYZZJY817TRQp%2FG3XEOjnLfAH5g%3D%3D&version=1.0&app_id=9021000123613164&sign_type=RSA2&timestamp=2023-07-30+23%3A11%3A17&alipay_sdk=alipay-sdk-java-dynamicVersionNo&format=json">
		 * <input type="hidden" name="biz_content" value="{&quot;out_trade_no&quot;:&quot;202307302206&quot;,&quot;product_code&quot;:&quot;FAST_INSTANT_TRADE_PAY&quot;,&quot;subject&quot;:&quot;iPhone&quot;,&quot;total_amount&quot;:&quot;12.56&quot;}">
		 * <input type="submit" value="立即支付" style="display:none" >
		 * </form>
		 * <script>document.forms[0].submit();</script>
		 */
		httpResponse.setContentType("text/html;charset=UTF-8"); // 设置头部
		httpResponse.getWriter().write(form);// 直接将完整的表单html输出到页面
		httpResponse.getWriter().flush();
		httpResponse.getWriter().close();
	}

	/**
	 * 支付宝异步回调
	 * request.setNotifyUrl("");
	 * http://localhost:9090/api/ali/notify
	 * 上面这个网址用内网穿透代理一下
	 * http://jqdxgm.natappfree.cc/api/ali/notify
	 * 此时支付宝就能回调到这个页面
	 */
	@PostMapping("/notify")
	public void aliPayNotify(HttpServletRequest request) throws AlipayApiException {
		log.debug(">>>>>>>支付宝异步回调");
		Map<String, String[]> parameterMap = request.getParameterMap();
		// [gmt_create, charset, gmt_payment, notify_time, subject, sign, buyer_id, invoice_amount, version, notify_id,
		// fund_bill_list, notify_type, out_trade_no, total_amount, trade_status, trade_no, auth_app_id, receipt_amount,
		// point_amount, buyer_pay_amount, app_id, sign_type, seller_id]
		Set<String> keySet = parameterMap.keySet(); // 获取http请求里面的所有键

		/**
		 * paramsMap如下：
		 * {gmt_create=2023-07-31 15:54:11, charset=UTF8,
		 * gmt_payment=2023-07-31 15:54:26,
		 * notify_time=2023-07-31 15:54:27,
		 * sign=fsCABNzc5hax4mwwMULluDiEAT70Kqj77uTMcCgSi82AU6tP5LGbXucEvP7CbvjXrYo5g3hrz5xRQAwddE7qU9Qyrg0v3EnearJBcW4It6N+VNBQ7yfY/W79eKRSKspLBKHRa21RILjyRrmQYG4Cw8R7twP7y0lDCOE8j3rV6ZyGfhiQ7EXp49d6HpgcIT1NjgJjQYSyJFdgyqkzFljKRfbhwPFAtubsmd8IcJCU7XI3YosSKnDhQaCA6ec4dmQiWtvcTbOLNR/r2Sou7rCnI7s1lc8pKeEsuacWTZW2FVR7hdHoLZ/expaRQIt+dNzA86lwQxu3SRCQ9wNTPICv1A==,
		 * buyer_id=2088722005286475, invoice_amount=12.56, version=1.0, notify_id=2023073101222155427086470500776505,
		 * fund_bill_list=[{"amount":"12.56","fundChannel":"ALIPAYACCOUNT"}],
		 * notify_type=trade_status_sync,
		 * subject=iPhone, // 物品名称
		 * out_trade_no=202307311553, // 进行支付的订单id，唯一，商家自定义，即支付时传入的 String orderId
		 * total_amount=12.56, // 总价格
		 * trade_status=TRADE_SUCCESS, // 支付状态
		 * trade_no=2023073122001486470500697216, auth_app_id=9021000123613164,
		 * receipt_amount=12.56, point_amount=0.00,
		 * buyer_pay_amount=12.56, app_id=9021000123613164, sign_type=RSA2, seller_id=2088721005318559}
		 */
		Map<String, String> paramsMap = new HashMap<>(); // 专门用来放置请求里面的参数
		for (String key : keySet) {
			paramsMap.put(key, request.getParameter(key));
		}
		System.out.println("*************");
		System.out.println(paramsMap);

		// 验证签名
		String sign = paramsMap.get("sign");
		String contentV1 = AlipaySignature.getSignCheckContentV1(paramsMap);
		boolean rsa256CheckSignature = AlipaySignature.rsa256CheckContent(contentV1, sign, alipayPublicKey, "UTF-8");
		if (rsa256CheckSignature && "TRADE_SUCCESS".equals(paramsMap.get("trade_status"))) {
			// 签名验证成功 并且 支付宝回调的状态是成功状态 TRADE_SUCCESS
			log.info("在{}，买家{}进行订单{}的付款，交易名称{}，付款金额{}",
					paramsMap.get("gmt_payment"), paramsMap.get("buyer_id"), paramsMap.get("out_trade_no"), paramsMap.get("subject"), paramsMap.get("total_amount"));

			// 支付成功，修改数据库中该订单的状态
			// 比如：流程，根据订单ID查询出一条数据，修改该条订单的数据，或者只有支付成功，才给数据库里面新增一条数据
		}
	}
}
