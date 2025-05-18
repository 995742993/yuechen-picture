package com.xwz.xwzpicturebackend.manager.message;

import com.xwz.xwzpicturebackend.exception.BusinessException;
import com.xwz.xwzpicturebackend.exception.ErrorCode;
import com.xwz.xwzpicturebackend.utils.EmailUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;

/**
 * @author 度星希
 * @createTime 2025/5/17 16:35
 * @description 邮件服务
 */
@Component
public class EmailManager {
	@Resource
	private JavaMailSender javaMailSender;

	@Value("${spring.mail.nickname}")
	private String nickname;

	@Value("${spring.mail.username}")
	private String from;

	public void sendEmail(String to, String subject, Map<String, Object> contentMap) {
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			// 组合邮箱发送的内容
			MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
			// 设置邮件发送者
			messageHelper.setFrom(nickname + "<" + from + ">");
			// 设置邮件接收者
			messageHelper.setTo(to);
			// 设置邮件标题
			messageHelper.setSubject(subject);
			// 设置邮件内容
			messageHelper.setText(EmailUtils.emailContentTemplate("templates/EmailCodeTemplate.html", contentMap), true);
			javaMailSender.send(message);
		} catch (MessagingException e) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送邮件失败");
		}
	}
}
