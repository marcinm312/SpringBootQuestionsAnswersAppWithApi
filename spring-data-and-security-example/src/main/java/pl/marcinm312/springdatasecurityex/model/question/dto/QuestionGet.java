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
}
