package pl.marcinm312.springdatasecurityex.shared.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailService {

	private final JavaMailSender javaMailSender;

	@Value("${spring.mail.username}")
	private String emailFrom;

	public void sendMail(String to, String subject, String text, boolean isHtmlContent) throws MessagingException {
		log.info("Starting creating an email");
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
		mimeMessageHelper.setFrom(emailFrom);
		mimeMessageHelper.setTo(to);
		mimeMessageHelper.setSubject(subject);
		mimeMessageHelper.setText(text, isHtmlContent);
		log.info("mail to = {}", to);
		log.info("mail subject = {}", subject);
		log.info("mail text = {}", text);
		log.info("mail isHtmlContent = {}", isHtmlContent);
		log.info("Starting sending an email");
		javaMailSender.send(mimeMessage);
		log.info("The mail has been sent");
	}
}
