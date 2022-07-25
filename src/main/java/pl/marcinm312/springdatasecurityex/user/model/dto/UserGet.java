package pl.marcinm312.springdatasecurityex.user.model.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.marcinm312.springdatasecurityex.shared.enums.Role;
import pl.marcinm312.springdatasecurityex.shared.model.CommonsDTOFields;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserGet extends CommonsDTOFields {

	private String username;
	private Role role;
	private boolean enabled;
	private String email;


	@Override
	public String toString() {
		return "UserGet{" +
				"username='" + username + '\'' +
				", role='" + role + '\'' +
				", enabled=" + enabled +
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
