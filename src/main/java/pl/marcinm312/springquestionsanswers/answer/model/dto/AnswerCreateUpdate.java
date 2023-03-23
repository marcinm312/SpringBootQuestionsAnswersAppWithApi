package pl.marcinm312.springquestionsanswers.answer.model.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AnswerCreateUpdate {

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 3, message = "Pole to musi zawierać minimum 3 znaki")
	private String text;

}
