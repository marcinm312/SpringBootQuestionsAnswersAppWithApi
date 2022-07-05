package pl.marcinm312.springdatasecurityex.answer.model.dto;

import lombok.Getter;
import lombok.Setter;
import pl.marcinm312.springdatasecurityex.shared.model.CommonsDTOFields;

@Getter
@Setter
public class AnswerGet extends CommonsDTOFields {

	private String text;
	private String user;

	@Override
	public String toString() {
		return "AnswerGet{" +
				"text='" + text + '\'' +
				", user='" + user + '\'' +
				"} " + super.toString();
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AnswerGet)) return false;

		AnswerGet answerGet = (AnswerGet) o;

		if (getText() != null ? !getText().equals(answerGet.getText()) : answerGet.getText() != null) return false;
		if (getId() != null ? !getId().equals(answerGet.getId()) : answerGet.getId() != null) return false;
		return getUser() != null ? getUser().equals(answerGet.getUser()) : answerGet.getUser() == null;
	}

	@Override
	public final int hashCode() {
		int result = getText() != null ? getText().hashCode() : 0;
		result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
		result = 31 * result + (getId() != null ? getId().hashCode() : 0);
		return result;
	}
}
