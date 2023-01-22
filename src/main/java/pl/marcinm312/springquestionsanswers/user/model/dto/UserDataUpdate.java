package pl.marcinm312.springquestionsanswers.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@Getter
@Setter
public class UserDataUpdate {

	@Schema(description = "The value should be unique")
	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 3, max = 50, message = "Pole to musi zawierać od 3 do 50 znaków")
	private String username;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Email(message = "Niepoprawny adres email!")
	private String email;


	@Override
	public String toString() {
		return "UserDataUpdate{" +
				"username='" + username + '\'' +
				'}';
	}
}
