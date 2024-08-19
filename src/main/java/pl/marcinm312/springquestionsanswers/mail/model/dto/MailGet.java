package pl.marcinm312.springquestionsanswers.mail.model.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.marcinm312.springquestionsanswers.shared.model.CommonsDTOFields;

import java.util.Objects;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MailGet extends CommonsDTOFields {

	private String to;
	private String subject;
	private boolean isHtmlContent;

	@Override
	public String toString() {
		return "MailGet{" +
				"to='" + to + '\'' +
				", subject='" + subject + '\'' +
				", isHtmlContent=" + isHtmlContent +
				"} " + super.toString();
	}

	@Override
	public final boolean equals(Object o) {

		if (this == o) return true;
		if (!(o instanceof MailGet mailGet)) return false;

		return isHtmlContent() == mailGet.isHtmlContent() && Objects.equals(getTo(), mailGet.getTo())
				&& Objects.equals(getSubject(), mailGet.getSubject()) && Objects.equals(getId(), mailGet.getId());
	}

	@Override
	public int hashCode() {

		int result = Objects.hashCode(getTo());
		result = 31 * result + Objects.hashCode(getSubject());
		result = 31 * result + Boolean.hashCode(isHtmlContent());
		result = 31 * result + Objects.hashCode(getId());
		return result;
	}
}
