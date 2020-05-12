package pl.marcinm312.springdatasecurityex.validator;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;

@Component
public class PasswordUpdateValidator implements Validator {

	private UserRepo userRepo;
	private PasswordEncoder passwordEncoder;

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
		Optional<User> foundUser = userRepo.findByUsername(username);
		if (foundUser.isPresent()) {
			if (!foundUser.get().getId().equals(user.getId())) {
				errors.rejectValue("username", "user_exists_error", "Użytkownik o takim loginie już istnieje!");
			}
		}

		String currentPassword = user.getCurrentPassword();
		String password = user.getPassword();
		String confirmPassword = user.getConfirmPassword();

		if (currentPassword.length() > 0 == false) {
			errors.rejectValue("currentPassword", "confirm_password_error", "Pole to musi być wypełnione!");
		}

		if (!passwordEncoder.matches(currentPassword, foundUser.get().getPassword())) {
			errors.rejectValue("currentPassword", "current_password_error", "Podano nieprawidłowe hasło");
		}

		if (currentPassword.equals(password)) {
			errors.rejectValue("password", "password_without_change", "Nowe hasło musi być inne od poprzedniego!");
		}

		if (!password.equals(confirmPassword)) {
			errors.rejectValue("password", "confirm_password_error", "Hasła w obu polach muszą być takie same!");
		}
	}
}
