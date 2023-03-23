package pl.marcinm312.springquestionsanswers.question.model.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class QuestionCreateUpdate {

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 3, max = 100, message = "Pole to musi zawierać od 3 do 100 znaków")
	private String title;

	private String description;

}
