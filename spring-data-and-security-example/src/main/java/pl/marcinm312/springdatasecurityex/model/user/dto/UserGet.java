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

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof UserGet)) return false;

		UserGet userGet = (UserGet) o;

		if (isEnabled() != userGet.isEnabled()) return false;
		if (getUsername() != null ? !getUsername().equals(userGet.getUsername()) : userGet.getUsername() != null)
			return false;
		if (getRole() != null ? !getRole().equals(userGet.getRole()) : userGet.getRole() != null) return false;
		if (getId() != null ? !getId().equals(userGet.getId()) : userGet.getId() != null) return false;
		return getEmail() != null ? getEmail().equals(userGet.getEmail()) : userGet.getEmail() == null;
	}

	@Override
	public final int hashCode() {
		int result = getUsername() != null ? getUsername().hashCode() : 0;
		result = 31 * result + (getRole() != null ? getRole().hashCode() : 0);
		result = 31 * result + (isEnabled() ? 1 : 0);
		result = 31 * result + (getEmail() != null ? getEmail().hashCode() : 0);
		result = 31 * result + (getId() != null ? getId().hashCode() : 0);
		return result;
	}
}
