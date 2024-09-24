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
import pl.marcinm312.springquestionsanswers.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailService {

	private static final String MAIL_NOT_FOUND = "No email found for id: ";

	private final JavaMailSender javaMailSender;
	private final MailRepository mailRepository;

	@Value("${mail.from}")
	private String emailFrom;

	@Async(value = "mailExecutor")
	@Retryable(retryFor = RuntimeMailException.class, maxAttemptsExpression = "${mail.max-attempts}",
			backoff = @Backoff(delayExpression = "${mail.delay} * 1000"))
	public Future<Boolean> sendMailAsync(String to, String subject, String text, boolean isHtmlContent) {
		sendMail(to, subject, text, isHtmlContent);
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		future.complete(true);
		return future;
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
			if (processOneMail(mail)) {
				processedSuccessfully++;
			} else {
				processedWithErrors++;
			}
		}

		log.info("All mails processed");
		return new MailRetryResult(mailsToRetry.size(), processedSuccessfully, processedWithErrors);
	}

	public MailGet getOneMailToRetry(Long mailId) {

		MailEntity mailEntity = getOneMailEntity(mailId);
		return MailMapper.convertMailEntityToMailGet(mailEntity);
	}

	public boolean retryOneMail(Long mailId) {

		MailEntity mailEntity = getOneMailEntity(mailId);
		resendMail(mailEntity);
		return true;
	}

	public boolean deleteOneMail(Long mailId) {

		MailEntity mailEntity = getOneMailEntity(mailId);
		mailRepository.delete(mailEntity);
		return true;
	}

	private boolean processOneMail(MailEntity mailEntity) {

		try {
			resendMail(mailEntity);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void resendMail(MailEntity mailEntity) {

		sendMail(mailEntity.getEmailRecipient(), mailEntity.getSubject(), mailEntity.getText(),
				mailEntity.isHtmlContent());
		mailRepository.delete(mailEntity);
	}

	private List<MailEntity> getMailEntitiesToRetry() {

		log.info("Loading mails");
		List<MailEntity> mails = mailRepository.findAll();
		log.info("{} mails returned for retry", mails.size());
		return mails;
	}

	private MailEntity getOneMailEntity(Long mailId) {

		return mailRepository.findById(mailId).orElseThrow(
				() -> new ResourceNotFoundException(MAIL_NOT_FOUND + mailId)
		);
	}
}
