package pl.marcinm312.springdatasecurityex.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserCreate;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;

import java.util.Optional;

@Component
public class UserCreateValidator implements Validator {

	private static final String USERNAME_FIELD = "username";
	private static final String PASSWORD_FIELD = "password";
	private static final String CONFIRM_PASSWORD_FIELD = "confirmPassword";

	private static final String USER_EXISTS_ERROR = "user_exists_error";
	private static final String CONFIRM_PASSWORD_ERROR = "confirm_password_error";
	private static final String START_END_SPACE_ERROR = "start_end_space_error";

	private final UserManager userManager;

	@Autowired
	public UserCreateValidator(UserManager userManager) {
		this.userManager = userManager;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return UserCreate.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UserCreate user = (UserCreate) target;

		String password = user.getPassword();
		String confirmPassword = user.getConfirmPassword();

		if (password.startsWith(" ") || password.endsWith(" ")) {
			errors.rejectValue(PASSWORD_FIELD, START_END_SPACE_ERROR, "Hasło nie może się zaczynać lub kończyć spacją");
		}

		if (confirmPassword.startsWith(" ") || confirmPassword.endsWith(" ")) {
			errors.rejectValue(CONFIRM_PASSWORD_FIELD, START_END_SPACE_ERROR, "Hasło nie może się zaczynać lub kończyć spacją");
		}

		String username = user.getUsername();
		Optional<User> foundUser = userManager.findUserByUsername(username);
		if (foundUser.isPresent()) {
			errors.rejectValue(USERNAME_FIELD, USER_EXISTS_ERROR, "Użytkownik o takim loginie już istnieje!");
		}

		if (!password.equals(confirmPassword)) {
			errors.rejectValue(CONFIRM_PASSWORD_FIELD, CONFIRM_PASSWORD_ERROR, "Hasła w obu polach muszą być takie same!");
		}
	}

}
