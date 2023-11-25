package com.example.studypartner.service;


import javax.mail.MessagingException;

/**
 * @author wuxie
 * @date 2023/10/17 9:14
 * @description 该文件的描述 todo
 */
public interface MailService {

	void sendMail(String to, String content) throws MessagingException;
}
