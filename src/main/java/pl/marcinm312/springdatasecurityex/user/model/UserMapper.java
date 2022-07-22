package pl.marcinm312.springdatasecurityex.user.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserGet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

	public static UserGet convertUserToUserGet(UserEntity user) {
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
