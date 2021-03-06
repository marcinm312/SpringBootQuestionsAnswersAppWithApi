package pl.marcinm312.springdatasecurityex.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserPasswordUpdate;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;

@Component
public class UserPasswordUpdateValidator implements Validator {

	private static final String CURRENT_PASSWORD_FIELD = "currentPassword";
	private static final String CONFIRM_PASSWORD_ERROR = "confirm_password_error";
	private static final String CURRENT_PASSWORD_ERROR = "current_password_error";
	private static final String PASSWORD_FIELD = "password";
	private static final String PASSWORD_WITHOUT_CHANGE_ERROR = "password_without_change";
	private static final String CONFIRM_PASSWORD_FIELD = "confirmPassword";

	private final UserManager userManager;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public UserPasswordUpdateValidator(UserManager userManager, PasswordEncoder passwordEncoder) {
		this.userManager = userManager;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return UserPasswordUpdate.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UserPasswordUpdate user = (UserPasswordUpdate) target;

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User loggedUser = userManager.getUserByAuthentication(authentication);

		String currentPassword = user.getCurrentPassword();
		String password = user.getPassword();
		String confirmPassword = user.getConfirmPassword();

		if (!passwordEncoder.matches(currentPassword, loggedUser.getPassword())) {
			errors.rejectValue(CURRENT_PASSWORD_FIELD, CURRENT_PASSWORD_ERROR, "Podano nieprawidłowe hasło");
		}

		if (currentPassword.equals(password)) {
			errors.rejectValue(PASSWORD_FIELD, PASSWORD_WITHOUT_CHANGE_ERROR, "Nowe hasło musi być inne od poprzedniego!");
		}

		if (!password.equals(confirmPassword)) {
			errors.rejectValue(CONFIRM_PASSWORD_FIELD, CONFIRM_PASSWORD_ERROR, "Hasła w obu polach muszą być takie same!");
		}
	}
}
