package pl.marcinm312.springdatasecurityex.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CommonsDTOFields {

	private Long id;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Date createdAt;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Date updatedAt;


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
