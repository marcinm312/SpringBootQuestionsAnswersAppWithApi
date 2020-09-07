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
			errors.rejectValue("currentPassword", "confirm_password_error", "Pole to musi być wypełnione!");
		}

		if (optionalUser.isPresent()) {
			if (!passwordEncoder.matches(currentPassword, optionalUser.get().getPassword())) {
				errors.rejectValue("currentPassword", "current_password_error", "Podano nieprawidłowe hasło");
			}
		} else {
			errors.rejectValue("currentPassword", "user_not_exists", "Użytkownik nie istnieje!");
		}

		if (currentPassword.equals(password)) {
			errors.rejectValue("password", "password_without_change", "Nowe hasło musi być inne od poprzedniego!");
		}

		if (!password.equals(confirmPassword)) {
			errors.rejectValue("confirmPassword", "confirm_password_error", "Hasła w obu polach muszą być takie same!");
		}
	}
}
