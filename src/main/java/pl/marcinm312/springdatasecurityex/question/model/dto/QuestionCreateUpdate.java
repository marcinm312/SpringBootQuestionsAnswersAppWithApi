package pl.marcinm312.springdatasecurityex.question.model.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class QuestionCreateUpdate {

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 3, max = 100, message = "Pole to musi zawierać od 3 do 100 znaków")
	private String title;

	private String description;


	public QuestionCreateUpdate() {

	}

	public QuestionCreateUpdate(String title, String description) {
		this.title = title;
		this.description = description;
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

	@Override
	public String toString() {
		return "QuestionCreateUpdate{" +
				"title='" + title + '\'' +
				", description='" + description + '\'' +
				'}';
	}
}
