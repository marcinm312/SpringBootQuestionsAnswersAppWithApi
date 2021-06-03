package pl.marcinm312.springdatasecurityex.model.question.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

	@JsonIgnore
	public String getUpdatedAtAsString() {
		return getUpdatedAt().toString().substring(0, 19).replace("T", " ");
	}

	@JsonIgnore
	public String getCreatedAtAsString() {
		return getCreatedAt().toString().substring(0, 19).replace("T", " ");
	}

	@Override
	public String toString() {
		return "QuestionGet{" +
				"id=" + getId() +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", createdAt=" + getCreatedAt() +
				", updatedAt=" + getUpdatedAt() +
				", user='" + user + '\'' +
				'}';
	}
}
