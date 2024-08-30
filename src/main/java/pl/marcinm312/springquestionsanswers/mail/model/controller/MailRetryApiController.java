package pl.marcinm312.springquestionsanswers.mail.model.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.marcinm312.springquestionsanswers.mail.model.dto.MailGet;
import pl.marcinm312.springquestionsanswers.mail.model.dto.MailRetryResult;
import pl.marcinm312.springquestionsanswers.mail.service.MailService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/mailsRetry")
public class MailRetryApiController {

	private final MailService mailService;

	@GetMapping("/getMailsToRetry")
	public List<MailGet> getMailsToRetry() {
		return mailService.getMailsToRetry();
	}

	@PostMapping("/retryAllMails")
	public MailRetryResult retryAllMails() {
		return mailService.retryAllMails();
	}
}
