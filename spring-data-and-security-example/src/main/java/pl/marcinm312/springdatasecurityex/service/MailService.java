package pl.marcinm312.springdatasecurityex.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

	private JavaMailSender javaMailSender;

	protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public MailService(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	public void sendMail(String to, String subject, String text, boolean isHtmlContent) throws MessagingException {
		log.info("Starting creating an email");
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
		mimeMessageHelper.setTo(to);
		mimeMessageHelper.setSubject(subject);
		mimeMessageHelper.setText(text, isHtmlContent);
		log.info("mail to = " + to);
		log.info("mail subject = " + subject);
		log.info("mail text = " + text);
		log.info("mail isHtmlContent = " + isHtmlContent);
		log.info("Starting sending an email");
		javaMailSender.send(mimeMessage);
		log.info("The mail has been sent");
	}
}