package pl.marcinm312.springquestionsanswers.mail.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MailRetryResult {

	private Integer mailsToProcess;
	private Integer processedSuccessfully;
	private Integer processedWithErrors;
}
