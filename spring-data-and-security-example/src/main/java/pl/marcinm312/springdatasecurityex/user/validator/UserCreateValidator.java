package pl.marcinm312.springdatasecurityex.user.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import pl.marcinm312.springdatasecurityex.user.model.User;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserCreate;
import pl.marcinm312.springdatasecurityex.user.service.UserDetailsServiceImpl;

import java.util.Optional;

@Component
public class UserCreateValidator implements Validator {

	private static final String USERNAME_FIELD = "username";
	private static final String CONFIRM_PASSWORD_FIELD = "confirmPassword";

	private static final String USER_EXISTS_ERROR = "user_exists_error";
	private static final String CONFIRM_PASSWORD_ERROR = "confirm_password_error";

	private final UserDetailsServiceImpl userDetailsService;

	@Autowired
	public UserCreateValidator(UserDetailsServiceImpl userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return UserCreate.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UserCreate user = (UserCreate) target;

		String username = user.getUsername();
		String password = user.getPassword();
		String confirmPassword = user.getConfirmPassword();

		Optional<User> foundUser = userDetailsService.findUserByUsername(username);
		if (foundUser.isPresent()) {
			errors.rejectValue(USERNAME_FIELD, USER_EXISTS_ERROR, "Użytkownik o takim loginie już istnieje!");
		}

		if (!password.equals(confirmPassword)) {
			errors.rejectValue(CONFIRM_PASSWORD_FIELD, CONFIRM_PASSWORD_ERROR, "Hasła w obu polach muszą być takie same!");
		}
	}

}
