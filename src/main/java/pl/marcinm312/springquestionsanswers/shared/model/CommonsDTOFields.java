package pl.marcinm312.springquestionsanswers.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@SuperBuilder
public class CommonsDTOFields {

	private Long id;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private LocalDateTime createdAt;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private LocalDateTime updatedAt;

	@JsonIgnore
	private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
