package pl.marcinm312.springdatasecurityex.model.user;

import pl.marcinm312.springdatasecurityex.model.user.dto.UserGet;

public class UserMapper {

	private UserMapper() {

	}

	public static UserGet convertUserToUserGet(User user) {
		UserGet userGet = new UserGet();
		userGet.setId(user.getId());
		userGet.setCreatedAt(user.getCreatedAt());
		userGet.setUpdatedAt(user.getUpdatedAt());
		userGet.setUsername(user.getUsername());
		userGet.setEmail(user.getEmail());
		userGet.setRole(user.getRole());
		userGet.setEnabled(user.isEnabled());
		return userGet;
	}
}
