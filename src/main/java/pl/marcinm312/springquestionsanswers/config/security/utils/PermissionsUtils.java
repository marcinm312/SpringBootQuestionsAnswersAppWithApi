package pl.marcinm312.springquestionsanswers.config.security.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.marcinm312.springquestionsanswers.shared.enums.Role;
import pl.marcinm312.springquestionsanswers.shared.model.CommonEntityWithUser;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PermissionsUtils {

	public static boolean checkIfUserIsPermitted (CommonEntityWithUser entityWithUser, UserEntity loggedUser) {

		Long objectUserId = entityWithUser.getUser().getId();
		Long currentUserId = loggedUser.getId();
		Role currentUserRole = loggedUser.getRole();
		log.info("userIdFromEntity={}, objectClass={}", objectUserId, entityWithUser.getClass().getName());
		log.info("currentUserId={}", currentUserId);
		log.info("currentUserRole={}", currentUserRole);
		return objectUserId.equals(currentUserId) || Role.ROLE_ADMIN == currentUserRole;
	}
}
