package pl.marcinm312.springdatasecurityex.user.model.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UserPasswordUpdate {

	@NotBlank(message = "Pole to musi być wypełnione!")
	private String currentPassword;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 6, message = "Pole to musi zawierać minimum 6 znaków")
	private String password;

	@NotBlank(message = "Pole to musi być wypełnione!")
	@Size(min = 6, message = "Pole to musi zawierać minimum 6 znaków")
	private String confirmPassword;

	public UserPasswordUpdate() {

	}

	public UserPasswordUpdate(String currentPassword, String password, String confirmPassword) {
		this.currentPassword = currentPassword;
		this.password = password;
		this.confirmPassword = confirmPassword;
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

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}
}
