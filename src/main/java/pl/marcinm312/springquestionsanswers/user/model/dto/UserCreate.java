package pl.marcinm312.springquestionsanswers.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserCreate {

	@Schema(description = "The value should be unique")
	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 3, max = 50, message = "Pole to musi zawierać od 3 do 50 znaków")
	private String username;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 5, message = "Pole to musi zawierać minimum 5 znaków")
	private String password;

	@Schema(description = "The value should be the same as in the `password` field")
	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 5, message = "Pole to musi zawierać minimum 5 znaków")
	private String confirmPassword;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Email(message = "Niepoprawny adres email!")
	private String email;

	@Schema(description = "Sets a link to activate the user account in the email. " +
			"You can set null value when using the default built-in application frontend. " +
			"If the link in the email should redirect to another page, enter its address, " +
			"e.g. `http://localhost:3000/api/token?value=`. The value of the token (in this case, " +
			"the value of the \"value\" parameter) will be added by the application to the end of the URL.")
	private String activationUrl;


	public UserCreate(String username, String password, String confirmPassword, String email) {
		this.username = username;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.email = email;
	}

	@Override
	public String toString() {
		return "UserCreate{" +
				"username='" + username + '\'' +
				", activationUrl='" + activationUrl + '\'' +
				'}';
	}
}
