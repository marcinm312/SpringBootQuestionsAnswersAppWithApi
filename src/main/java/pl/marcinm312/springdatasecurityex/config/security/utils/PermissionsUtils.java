package pl.marcinm312.springdatasecurityex.config.security.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import pl.marcinm312.springdatasecurityex.shared.enums.Roles;
import pl.marcinm312.springdatasecurityex.shared.model.CommonEntityWithUser;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

@UtilityClass
@Slf4j
public class PermissionsUtils {

	public static boolean checkIfUserIsPermitted (CommonEntityWithUser entityWithUser, UserEntity loggedUser) {

		Long objectUserId = entityWithUser.getUser().getId();
		Long currentUserId = loggedUser.getId();
		String currentUserRole = loggedUser.getRole();
		log.info("userIdFromEntity={}, objectClass={}", objectUserId, entityWithUser.getClass().getName());
		log.info("currentUserId={}", currentUserId);
		log.info("currentUserRole={}", currentUserRole);
		return objectUserId.equals(currentUserId) || Roles.ROLE_ADMIN.name().equals(currentUserRole);
	}
}
