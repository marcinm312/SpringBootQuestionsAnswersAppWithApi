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
import pl.marcinm312.springquestionsanswers.mail.model.MailEntity;
import pl.marcinm312.springquestionsanswers.mail.model.MailMapper;
import pl.marcinm312.springquestionsanswers.mail.model.dto.MailGet;
import pl.marcinm312.springquestionsanswers.mail.model.dto.MailRetryResult;
import pl.marcinm312.springquestionsanswers.mail.repository.MailRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailService {

	private final JavaMailSender javaMailSender;
	private final MailRepository mailRepository;

	@Value("${spring.mail.username}")
	private String emailFrom;

	@Async(value = "mailExecutor")
	@Retryable(retryFor = RuntimeMailException.class, maxAttemptsExpression = "${mail.max-attempts}",
			backoff = @Backoff(delayExpression = "${mail.delay} * 1000"))
	public void sendMailAsync(String to, String subject, String text, boolean isHtmlContent) {
		sendMail(to, subject, text, isHtmlContent);
	}

	private void sendMail(String to, String subject, String text, boolean isHtmlContent) {

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		try {
			log.info("Sending email: to = {}, subject = {}, isHtmlContent = {}", to, subject, isHtmlContent);
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessageHelper.setFrom(emailFrom);
			mimeMessageHelper.setTo(to);
			mimeMessageHelper.setSubject(subject);
			mimeMessageHelper.setText(text, isHtmlContent);
			javaMailSender.send(mimeMessage);
			log.info("Email sent: to = {}, subject = {}, isHtmlContent = {}", to, subject, isHtmlContent);
		} catch (Exception e) {
			String errorMessage = String.format("An error occurred while sending the email. [MESSAGE]: %s", e.getMessage());
			log.error(errorMessage, e);
			throw new RuntimeMailException(errorMessage);
		}
	}

	@Recover
	private void handleMailException(RuntimeMailException e, String to, String subject, String text,
									boolean isHtmlContent) {

		log.error("Max attempts reached. Failed to send email. Error message: {}", e.getMessage());
		saveMail(to, subject, text, isHtmlContent);
	}

	private void saveMail(String to, String subject, String text, boolean isHtmlContent) {

		MailEntity mailEntity = new MailEntity(to, subject, text, isHtmlContent);
		log.info("Saving mail: {}", mailEntity);
		mailRepository.save(mailEntity);
	}

	public List<MailGet> getMailsToRetry() {
		return MailMapper.convertMailEntityListToMailGetList(getMailEntitiesToRetry());
	}

	public MailRetryResult retryAllMails() {

		List<MailEntity> mailsToRetry = getMailEntitiesToRetry();
		int processedSuccessfully = 0;
		int processedWithErrors = 0;

		for (MailEntity mail : mailsToRetry) {
			if (retryOneMail(mail)) {
				processedSuccessfully++;
			} else {
				processedWithErrors++;
			}
		}

		return new MailRetryResult(mailsToRetry.size(), processedSuccessfully, processedWithErrors);
	}

	private boolean retryOneMail(MailEntity mailEntity) {

		try {
			sendMail(mailEntity.getEmailRecipient(), mailEntity.getSubject(), mailEntity.getText(),
					mailEntity.isHtmlContent());
			mailRepository.delete(mailEntity);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private List<MailEntity> getMailEntitiesToRetry() {

		log.info("Loading mails");
		List<MailEntity> mails = mailRepository.findAll();
		log.info("{} mails returned for retry", mails.size());
		return mails;
	}
}
