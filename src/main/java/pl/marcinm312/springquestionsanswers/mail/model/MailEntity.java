package pl.marcinm312.springquestionsanswers.mail.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pl.marcinm312.springquestionsanswers.shared.model.AuditModel;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "mails")
public class MailEntity extends AuditModel {

	@Id
	@GeneratedValue(generator = "mail_generator")
	@SequenceGenerator(name = "mail_generator", sequenceName = "mail_sequence", initialValue = 1000,
			allocationSize = 1)
	private Long id;

	private String emailRecipient;
	private String subject;

	@Column(columnDefinition = "text")
	private String text;

	private boolean isHtmlContent;


	public MailEntity(String emailRecipient, String subject, String text, boolean isHtmlContent) {
		this.emailRecipient = emailRecipient;
		this.subject = subject;
		this.text = text;
		this.isHtmlContent = isHtmlContent;
	}

	@Override
	public String toString() {
		return "MailEntity{" +
				"emailRecipient='" + emailRecipient + '\'' +
				", subject='" + subject + '\'' +
				", isHtmlContent=" + isHtmlContent +
				'}';
	}
}
