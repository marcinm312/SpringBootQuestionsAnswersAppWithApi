package pl.marcinm312.springdatasecurityex.user.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserGet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

	public static UserGet convertUserToUserGet(UserEntity user, boolean isCreateOrUpdate) {

		var builder = UserGet.builder()
				.id(user.getId())
				.username(user.getUsername())
				.email(user.getEmail())
				.role(user.getRole())
				.enabled(user.isEnabled());

		if (!isCreateOrUpdate) {
			builder = builder
					.createdAt(user.getCreatedAt())
					.updatedAt(user.getUpdatedAt());
		}

		return builder.build();
	}
}
