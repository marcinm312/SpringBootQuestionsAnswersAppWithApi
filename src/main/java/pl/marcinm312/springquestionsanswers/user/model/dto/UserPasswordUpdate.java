package pl.marcinm312.springquestionsanswers.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserPasswordUpdate {

	@NotBlank(message = "Pole to musi być wypełnione!")
	private String currentPassword;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 5, message = "Pole to musi zawierać minimum 5 znaków")
	private String password;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 5, message = "Pole to musi zawierać minimum 5 znaków")
	private String confirmPassword;
}
