package pl.marcinm312.springquestionsanswers.mail.model.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.marcinm312.springquestionsanswers.mail.model.dto.MailGet;
import pl.marcinm312.springquestionsanswers.mail.service.MailRetryService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/mailsRetry")
public class MailRetryApiController {

	private final MailRetryService mailRetryService;

	@GetMapping
	public List<MailGet> getMailsToRetry() {
		return mailRetryService.getMailsToRetry();
	}
}
