package com.example.studypartner.service.impl;

import com.example.studypartner.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static com.example.studypartner.constant.EmailConstant.EMAIL_SUBJECT;


/**
 * @author wuxie
 * @date 2023年10月17日 09点19分
 * @description 邮箱发送
 */
@Service
public class MailServiceImpl implements MailService {
	@Autowired(required = false)
	private JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String userName;

	/**
	 * 发送信息邮件
	 *
	 * @param emailAccount 收件人
	 * @param captcha      邮箱内容
	 * @throws MessagingException
	 */
	public void sendMail(String emailAccount, String captcha) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		//邮箱发送者
		helper.setFrom(userName);
		//收件人，可以为多个收件人，收件人之间用逗号隔开
		helper.setTo(emailAccount);
		// 邮箱标题
		helper.setSubject(EMAIL_SUBJECT);
		// 邮箱内容
		helper.setText("你的验证码为: " + captcha + "，有效期为2分钟。", true);
		mailSender.send(message);

	}
}
