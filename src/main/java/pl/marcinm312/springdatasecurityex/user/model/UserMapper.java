package pl.marcinm312.springdatasecurityex.user.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserGet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

	public static UserGet convertUserToUserGet(UserEntity user) {

		return UserGet.builder()
				.id(user.getId())
				.createdAt(user.getCreatedAt())
				.updatedAt(user.getUpdatedAt())
				.username(user.getUsername())
				.email(user.getEmail())
				.role(user.getRole())
				.enabled(user.isEnabled())
				.build();
	}
}
