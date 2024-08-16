package pl.marcinm312.springquestionsanswers.user.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserGet;

import java.util.List;

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

	public static List<UserGet> convertUserEntityListToUserGetList(List<UserEntity> users) {
		return users.stream().map(user -> convertUserToUserGet(user, false)).toList();
	}
}
