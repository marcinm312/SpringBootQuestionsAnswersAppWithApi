package pl.marcinm312.springdatasecurityex.user.model.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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

	public UserCreate() {

	}

	public UserCreate(String username, String password, String confirmPassword, String email) {
		this.username = username;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "UserCreate{" +
				"username='" + username + '\'' +
				'}';
	}
}
