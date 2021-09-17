package pl.marcinm312.springdatasecurityex.utils;

import org.slf4j.LoggerFactory;
import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.model.answer.Answer;
import pl.marcinm312.springdatasecurityex.model.question.Question;
import pl.marcinm312.springdatasecurityex.model.user.User;

public class PermissionsUtils {

	private PermissionsUtils() {

	}

	public static boolean checkIfUserIsPermitted (Object object, User loggedUser) {
		org.slf4j.Logger log = LoggerFactory.getLogger(PermissionsUtils.class);
		Long objectUserId;
		if (object instanceof Question) {
			objectUserId = ((Question) object).getUser().getId();
		} else if (object instanceof Answer) {
			objectUserId = ((Answer) object).getUser().getId();
		} else {
			return false;
		}
		Long currentUserId = loggedUser.getId();
		String currentUserRole = loggedUser.getRole();
		log.info("objectUserId={}", objectUserId);
		log.info("currentUserId={}", currentUserId);
		log.info("currentUserRole={}", currentUserRole);
		return objectUserId.equals(currentUserId) || currentUserRole.equals(Roles.ROLE_ADMIN.name());
	}
}
