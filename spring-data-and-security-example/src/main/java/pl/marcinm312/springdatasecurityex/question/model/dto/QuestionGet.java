package pl.marcinm312.springdatasecurityex.question.model.dto;

import lombok.Getter;
import lombok.Setter;
import pl.marcinm312.springdatasecurityex.shared.model.CommonsDTOFields;

@Getter
@Setter
public class QuestionGet extends CommonsDTOFields {

	private String title;
	private String description;
	private String user;

	@Override
	public String toString() {
		return "QuestionGet{" +
				"title='" + title + '\'' +
				", description='" + description + '\'' +
				", user='" + user + '\'' +
				"} " + super.toString();
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof QuestionGet)) return false;

		QuestionGet questionGet = (QuestionGet) o;

		if (getTitle() != null ? !getTitle().equals(questionGet.getTitle()) : questionGet.getTitle() != null)
			return false;
		if (getDescription() != null ? !getDescription().equals(questionGet.getDescription()) : questionGet.getDescription() != null)
			return false;
		if (getId() != null ? !getId().equals(questionGet.getId()) : questionGet.getId() != null) return false;
		return getUser() != null ? getUser().equals(questionGet.getUser()) : questionGet.getUser() == null;
	}

	@Override
	public final int hashCode() {
		int result = getTitle() != null ? getTitle().hashCode() : 0;
		result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
		result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
		result = 31 * result + (getId() != null ? getId().hashCode() : 0);
		return result;
	}
}
