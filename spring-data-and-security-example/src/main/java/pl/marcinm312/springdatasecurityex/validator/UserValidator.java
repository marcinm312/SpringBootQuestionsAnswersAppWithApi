package pl.marcinm312.springdatasecurityex.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;

@Component
public class UserValidator implements Validator {

	private UserRepo userRepo;

	@Autowired
	public UserValidator(UserRepo userRepo) {
		this.userRepo = userRepo;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		User user = (User) target;

		String username = user.getUsername();
		if (userRepo.findByUsername(username).isPresent()) {
			errors.rejectValue("username", "user_exists_error", "Użytkownik o takim loginie już istnieje!");
		}

		String password = user.getPassword();
		String confirmPassword = user.getConfirmPassword();
		if (!password.equals(confirmPassword)) {
			errors.rejectValue("password", "confirm_password_error", "Hasła w obu polach muszą być takie same!");
		}
	}

}
