package pl.marcinm312.springdatasecurityex.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserDataUpdate;
import pl.marcinm312.springdatasecurityex.service.db.UserDetailsServiceImpl;

import java.util.Optional;

@Component
public class UserDataUpdateValidator implements Validator {

	private static final String USERNAME_FIELD = "username";
	private static final String USER_EXISTS_ERROR = "user_exists_error";
	private static final String LOGGED_USER_NOT_EXISTS = "logged_user_not_exists";

	private final UserDetailsServiceImpl userDetailsService;

	@Autowired
	public UserDataUpdateValidator(UserDetailsServiceImpl userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return UserDataUpdate.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UserDataUpdate user = (UserDataUpdate) target;

		String username = user.getUsername();
		Optional<User> foundUser = userDetailsService.findUserByUsername(username);

		String loggedUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> optionalLoggedUser = userDetailsService.findUserByUsername(loggedUserLogin);
		if (optionalLoggedUser.isPresent()) {
			User loggedUser = optionalLoggedUser.get();
			if (foundUser.isPresent() && !foundUser.get().getId().equals(loggedUser.getId())) {
				errors.rejectValue(USERNAME_FIELD, USER_EXISTS_ERROR, "Użytkownik o takim loginie już istnieje!");
			}
		} else {
			errors.rejectValue(USERNAME_FIELD, LOGGED_USER_NOT_EXISTS, "Zalogowany użytkownik nie istnieje w bazie danych!");
		}
	}

}
