package pl.marcinm312.springquestionsanswers.mail.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springquestionsanswers.mail.model.dto.MailGet;
import pl.marcinm312.springquestionsanswers.mail.model.dto.MailRetryResult;
import pl.marcinm312.springquestionsanswers.mail.service.MailService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/mailsToRetry")
public class MailRetryApiController {

	private final MailService mailService;

	@GetMapping
	public List<MailGet> getMailsToRetry() {
		return mailService.getMailsToRetry();
	}

	@GetMapping("/{mailId}")
	public MailGet getOneMailToRetry(@PathVariable Long mailId) {
		return mailService.getOneMailToRetry(mailId);
	}

	@PostMapping
	public MailRetryResult retryAllMails() {
		return mailService.retryAllMails();
	}

	@PostMapping("/{mailId}")
	public boolean retryOneMail(@PathVariable Long mailId) {
		return mailService.retryOneMail(mailId);
	}

	@DeleteMapping("/{mailId}")
	public boolean deleteOneMail(@PathVariable Long mailId) {
		return mailService.deleteOneMail(mailId);
	}
}
