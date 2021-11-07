package pl.marcinm312.springdatasecurityex.config.security.utils;

import org.slf4j.LoggerFactory;
import pl.marcinm312.springdatasecurityex.answer.model.AnswerEntity;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.shared.enums.Roles;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

public class PermissionsUtils {

	private PermissionsUtils() {

	}

	public static boolean checkIfUserIsPermitted (Object object, UserEntity loggedUser) {
		org.slf4j.Logger log = LoggerFactory.getLogger(PermissionsUtils.class);
		Long objectUserId;
		if (object instanceof QuestionEntity) {
			objectUserId = ((QuestionEntity) object).getUser().getId();
		} else if (object instanceof AnswerEntity) {
			objectUserId = ((AnswerEntity) object).getUser().getId();
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
