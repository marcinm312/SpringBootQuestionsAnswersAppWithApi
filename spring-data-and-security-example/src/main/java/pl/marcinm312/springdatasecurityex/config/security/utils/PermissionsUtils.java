package pl.marcinm312.springdatasecurityex.config.security.utils;

import org.slf4j.LoggerFactory;
import pl.marcinm312.springdatasecurityex.shared.enums.Roles;
import pl.marcinm312.springdatasecurityex.shared.model.EntityWithUser;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

public class PermissionsUtils {

	private PermissionsUtils() {

	}

	public static boolean checkIfUserIsPermitted (EntityWithUser entityWithUser, UserEntity loggedUser) {
		org.slf4j.Logger log = LoggerFactory.getLogger(PermissionsUtils.class);
		Long objectUserId = entityWithUser.getUser().getId();
		Long currentUserId = loggedUser.getId();
		String currentUserRole = loggedUser.getRole();
		log.info("userIdFromEntity={}, objectClass={}", objectUserId, entityWithUser.getClass().getName());
		log.info("currentUserId={}", currentUserId);
		log.info("currentUserRole={}", currentUserRole);
		return objectUserId.equals(currentUserId) || Roles.ROLE_ADMIN.name().equals(currentUserRole);
	}
}
