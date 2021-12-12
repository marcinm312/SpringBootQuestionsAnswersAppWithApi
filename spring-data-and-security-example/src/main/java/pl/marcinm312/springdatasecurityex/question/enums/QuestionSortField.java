package pl.marcinm312.springdatasecurityex.question.enums;

public enum QuestionSortField {

	ID("id"),
	TITLE("title"),
	DESCRIPTION("description"),
	CREATED_AT("createdAt"),
	UPDATED_AT("updatedAt"),
	USER("user.username");

	private final String field;

	QuestionSortField(String field) {
		this.field = field;
	}

	public String getField() {
		return field;
	}
}
