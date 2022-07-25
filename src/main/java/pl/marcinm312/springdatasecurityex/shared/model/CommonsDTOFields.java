package pl.marcinm312.springdatasecurityex.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@NoArgsConstructor
@SuperBuilder
public class CommonsDTOFields {

	private Long id;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Date createdAt;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Date updatedAt;

	@JsonIgnore
	private final Format dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@JsonIgnore
	public String getCreatedAtAsString() {
		return dateFormat.format(createdAt);
	}

	@JsonIgnore
	public String getUpdatedAtAsString() {
		return dateFormat.format(updatedAt);
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
