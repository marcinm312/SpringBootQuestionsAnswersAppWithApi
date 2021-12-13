package pl.marcinm312.springdatasecurityex.shared.filter;

public enum SortField {

	ID("id"),
	TITLE("title"),
	DESCRIPTION("description"),
	CREATED_AT("createdAt"),
	UPDATED_AT("updatedAt"),
	USER("user.username");

	private final String field;

	SortField(String field) {
		this.field = field;
	}

	public String getField() {
		return field;
	}
}
