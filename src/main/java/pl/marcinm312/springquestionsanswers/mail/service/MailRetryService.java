package pl.marcinm312.springquestionsanswers.mail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.marcinm312.springquestionsanswers.mail.model.MailEntity;
import pl.marcinm312.springquestionsanswers.mail.model.MailMapper;
import pl.marcinm312.springquestionsanswers.mail.model.dto.MailGet;
import pl.marcinm312.springquestionsanswers.mail.repository.MailRepository;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class MailRetryService {

	private final MailRepository mailRepository;


	public MailEntity saveMail(String to, String subject, String text, boolean isHtmlContent) {

		MailEntity mailEntity = new MailEntity(to, subject, text, isHtmlContent);
		log.info("Saving mail: {}", mailEntity);
		return mailRepository.save(mailEntity);
	}

	public List<MailGet> getMailsToRetry() {
		return MailMapper.convertMailEntityListToMailGetList(mailRepository.findAll());
	}
}
