package pl.marcinm312.springdatasecurityex.model.user.dto;

import pl.marcinm312.springdatasecurityex.model.CommonsDTOFields;

public class UserGet extends CommonsDTOFields {

	private String username;
	private String role;
	private boolean isEnabled;
	private String email;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "UserGet{" +
				"username='" + username + '\'' +
				", role='" + role + '\'' +
				", isEnabled=" + isEnabled +
				", email='" + email + '\'' +
				"} " + super.toString();
	}
}
