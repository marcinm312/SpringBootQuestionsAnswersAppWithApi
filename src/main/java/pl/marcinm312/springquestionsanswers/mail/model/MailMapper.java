package pl.marcinm312.springquestionsanswers.mail.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.marcinm312.springquestionsanswers.mail.model.dto.MailGet;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MailMapper {

	public static MailGet convertMailEntityToMailGet(MailEntity mail) {

		var builder = MailGet.builder()
				.id(mail.getId())
				.to(mail.getEmailRecipient())
				.subject(mail.getSubject())
				.htmlContent(mail.isHtmlContent())
				.createdAt(mail.getCreatedAt());

		return builder.build();
	}

	public static List<MailGet> convertMailEntityListToMailGetList(List<MailEntity> mails) {
		return mails.stream().map(MailMapper::convertMailEntityToMailGet).toList();
	}
}
