package pl.marcinm312.springdatasecurityex.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class CommonsDTOFields {

	private Long id;
	private Date createdAt;
	private Date updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
		return "CommonsDTOFields{" +
				"id=" + id +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				'}';
	}
}
