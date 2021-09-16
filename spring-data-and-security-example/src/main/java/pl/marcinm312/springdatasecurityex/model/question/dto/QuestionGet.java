package pl.marcinm312.springdatasecurityex.model.question.dto;

import pl.marcinm312.springdatasecurityex.model.CommonsDTOFields;

public class QuestionGet extends CommonsDTOFields {

	private String title;
	private String description;
	private String user;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

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
