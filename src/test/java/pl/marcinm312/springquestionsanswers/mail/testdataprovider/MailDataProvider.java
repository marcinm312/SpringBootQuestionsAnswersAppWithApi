package pl.marcinm312.springquestionsanswers.mail.testdataprovider;

import pl.marcinm312.springquestionsanswers.mail.model.MailEntity;
import pl.marcinm312.springquestionsanswers.shared.testdataprovider.DateProvider;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class MailDataProvider {

	public static List<MailEntity> prepareExampleMailsList() {

		List<MailEntity> mails = new ArrayList<>();
		mails.add(buildMailEntity(1000L, "test@abc.pl", "Test subject", "Test content",
				DateProvider.prepareDate(2024, Month.OCTOBER, 1, 14, 0, 0)));
		mails.add(buildMailEntity(1001L, "test2@abc.pl", "Test subject 2", "Test content 2",
				DateProvider.prepareDate(2024, Month.OCTOBER, 1, 14, 5, 0)));
		mails.add(buildMailEntity(1002L, null, "Test subject", "Test content",
				DateProvider.prepareDate(2024, Month.OCTOBER, 1, 14, 10, 0)));
		return mails;
	}

	public static MailEntity prepareExampleMail() {

		return buildMailEntity(1000L, "test@abc.pl", "Test subject", "Test content",
				DateProvider.prepareDate(2024, Month.OCTOBER, 1, 14, 0, 0));
	}

	private static MailEntity buildMailEntity(Long id, String emailRecipient, String subject, String text,
											  LocalDateTime createdAt) {

		return MailEntity.builder()
				.id(id)
				.emailRecipient(emailRecipient)
				.subject(subject)
				.text(text)
				.htmlContent(true)
				.createdAt(createdAt)
				.updatedAt(createdAt)
				.build();
	}
}
