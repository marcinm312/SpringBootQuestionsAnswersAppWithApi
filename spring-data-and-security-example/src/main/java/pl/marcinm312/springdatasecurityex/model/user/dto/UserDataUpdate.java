package pl.marcinm312.springdatasecurityex.model.user.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UserDataUpdate {

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 3, max = 50, message = "Pole to musi zawierać od 3 do 50 znaków")
	private String username;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Email(message = "Niepoprawny adres email!")
	private String email;

	public UserDataUpdate() {

	}

	public UserDataUpdate(String username, String email) {
		this.username = username;
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "UserDataUpdate{" +
				"username='" + username + '\'' +
				'}';
	}
}
