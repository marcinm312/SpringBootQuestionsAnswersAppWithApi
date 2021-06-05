package pl.marcinm312.springdatasecurityex.model.answer.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AnswerCreateUpdate {

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 3, message = "Pole to musi zawierać minimum 3 znaki")
	private String text;

	public AnswerCreateUpdate() {

	}

	public AnswerCreateUpdate(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
