package pl.marcinm312.springquestionsanswers.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserCreate {

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 3, max = 50, message = "Pole to musi zawierać od 3 do 50 znaków")
	private String username;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 6, message = "Pole to musi zawierać minimum 6 znaków")
	private String password;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 6, message = "Pole to musi zawierać minimum 6 znaków")
	private String confirmPassword;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Email(message = "Niepoprawny adres email!")
	private String email;


	@Override
	public String toString() {
		return "UserCreate{" +
				"username='" + username + '\'' +
				'}';
	}
}
