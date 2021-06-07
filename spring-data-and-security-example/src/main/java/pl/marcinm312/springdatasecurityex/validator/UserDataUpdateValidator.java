package pl.marcinm312.springdatasecurityex.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserDataUpdate;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;

import java.util.Optional;

@Component
public class UserDataUpdateValidator implements Validator {

	private static final String USERNAME_FIELD = "username";
	private static final String USER_EXISTS_ERROR = "user_exists_error";

	private final UserManager userManager;

	@Autowired
	public UserDataUpdateValidator(UserManager userManager) {
		this.userManager = userManager;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return UserDataUpdate.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UserDataUpdate user = (UserDataUpdate) target;

		String username = user.getUsername();
		Optional<User> foundUser = userManager.findUserByUsername(username);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User loggedUser = userManager.getUserByAuthentication(authentication);

		if (foundUser.isPresent() && !foundUser.get().getId().equals(loggedUser.getId())) {
			errors.rejectValue(USERNAME_FIELD, USER_EXISTS_ERROR, "Użytkownik o takim loginie już istnieje!");
		}
	}

}
