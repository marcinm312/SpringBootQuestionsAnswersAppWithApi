package pl.marcinm312.springquestionsanswers.mail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import pl.marcinm312.springquestionsanswers.mail.exception.RuntimeMailException;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailSendService {

	private final JavaMailSender javaMailSender;
	private final MailRetryService mailRetryService;

	@Value("${spring.mail.username}")
	private String emailFrom;

	@Async(value = "mailExecutor")
	@Retryable(retryFor = RuntimeMailException.class, maxAttemptsExpression = "${mail.max-attempts}",
			backoff = @Backoff(delayExpression = "${mail.delay} * 1000"))
	public void sendMail(String to, String subject, String text, boolean isHtmlContent) {

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessageHelper.setFrom(emailFrom);
			mimeMessageHelper.setTo(to);
			mimeMessageHelper.setSubject(subject);
			mimeMessageHelper.setText(text, isHtmlContent);
			log.info("Sending email: to = {}, subject = {}, isHtmlContent = {}", to, subject, isHtmlContent);
			javaMailSender.send(mimeMessage);
			log.info("Email sent: to = {}, subject = {}, isHtmlContent = {}", to, subject, isHtmlContent);
		} catch (Exception e) {
			String errorMessage = String.format("An error occurred while sending the email. [MESSAGE]: %s", e.getMessage());
			log.error(errorMessage, e);
			throw new RuntimeMailException(errorMessage);
		}
	}

	@Recover
	public void handleMailException(RuntimeMailException e, String to, String subject, String text,
									boolean isHtmlContent) {

		log.error("Max attempts reached. Failed to send email. Error message: {}", e.getMessage());
		mailRetryService.saveMail(to, subject, text, isHtmlContent);
	}
}
