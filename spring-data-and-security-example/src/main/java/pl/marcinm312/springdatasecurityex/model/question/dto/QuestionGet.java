package pl.marcinm312.springdatasecurityex.model.question.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class QuestionGet {

	private Long id;
	private String title;
	private String description;
	private Date createdAt;
	private Date updatedAt;
	private String user;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
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
				"id=" + id +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				", user='" + user + '\'' +
				'}';
	}
}
