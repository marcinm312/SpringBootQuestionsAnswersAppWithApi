package pl.marcinm312.springdatasecurityex.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;

import java.util.Optional;

@Component
public class PasswordUpdateValidator implements Validator {

	private static final String CURRENT_PASSWORD_FIELD = "currentPassword";
	private static final String CONFIRM_PASSWORD_ERROR = "confirm_password_error";
	private static final String CURRENT_PASSWORD_ERROR = "current_password_error";
	private static final String USER_NOT_EXISTS_ERROR = "user_not_exists";
	private static final String PASSWORD_FIELD = "password";
	private static final String PASSWORD_WITHOUT_CHANGE_ERROR = "password_without_change";
	private static final String CONFIRM_PASSWORD_FIELD = "confirmPassword";

	private final UserRepo userRepo;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public PasswordUpdateValidator(UserRepo userRepo, PasswordEncoder passwordEncoder) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		User user = (User) target;

		String username = user.getUsername();
		Optional<User> optionalUser = userRepo.findByUsername(username);

		String currentPassword = user.getCurrentPassword();
		String password = user.getPassword();
		String confirmPassword = user.getConfirmPassword();

		if (currentPassword.length() <= 0) {
			errors.rejectValue(CURRENT_PASSWORD_FIELD, CONFIRM_PASSWORD_ERROR, "Pole to musi być wypełnione!");
		}

		if (optionalUser.isPresent()) {
			if (!passwordEncoder.matches(currentPassword, optionalUser.get().getPassword())) {
				errors.rejectValue(CURRENT_PASSWORD_FIELD, CURRENT_PASSWORD_ERROR, "Podano nieprawidłowe hasło");
			}
		} else {
			errors.rejectValue(CURRENT_PASSWORD_FIELD, USER_NOT_EXISTS_ERROR, "Użytkownik nie istnieje!");
		}

		if (currentPassword.equals(password)) {
			errors.rejectValue(PASSWORD_FIELD, PASSWORD_WITHOUT_CHANGE_ERROR, "Nowe hasło musi być inne od poprzedniego!");
		}

		if (!password.equals(confirmPassword)) {
			errors.rejectValue(CONFIRM_PASSWORD_FIELD, CONFIRM_PASSWORD_ERROR, "Hasła w obu polach muszą być takie same!");
		}
	}
}
