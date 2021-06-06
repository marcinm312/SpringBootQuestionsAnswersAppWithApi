package pl.marcinm312.springdatasecurityex.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;

import java.util.Optional;

@Component
public class UserValidator implements Validator {

	private static final String USERNAME_FIELD = "username";
	private static final String USER_EXISTS_ERROR = "user_exists_error";
	private static final String CONFIRM_PASSWORD_FIELD = "confirmPassword";
	private static final String CONFIRM_PASSWORD_ERROR = "confirm_password_error";

	private final UserManager userManager;

	@Autowired
	public UserValidator(UserManager userManager) {
		this.userManager = userManager;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		User user = (User) target;

		String username = user.getUsername();
		Optional<User> foundUser = userManager.findUserByUsername(username);
		if (foundUser.isPresent() && !foundUser.get().getId().equals(user.getId())) {
			errors.rejectValue(USERNAME_FIELD, USER_EXISTS_ERROR, "Użytkownik o takim loginie już istnieje!");
		}

		String password = user.getPassword();
		String confirmPassword = user.getConfirmPassword();
		if (!password.equals(confirmPassword)) {
			errors.rejectValue(CONFIRM_PASSWORD_FIELD, CONFIRM_PASSWORD_ERROR, "Hasła w obu polach muszą być takie same!");
		}
	}

}
