package pl.marcinm312.springdatasecurityex.model.answer.dto;

import pl.marcinm312.springdatasecurityex.model.CommonsDTOFields;

public class AnswerGet extends CommonsDTOFields {

	private String text;
	private String user;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "AnswerGet{" +
				"text='" + text + '\'' +
				", user='" + user + '\'' +
				"} " + super.toString();
	}
}
