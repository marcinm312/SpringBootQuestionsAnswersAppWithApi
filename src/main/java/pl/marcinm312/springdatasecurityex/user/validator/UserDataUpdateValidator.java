package pl.marcinm312.springdatasecurityex.user.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserDataUpdate;
import pl.marcinm312.springdatasecurityex.user.service.UserDetailsServiceImpl;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserDataUpdateValidator implements Validator {

	private static final String USERNAME_FIELD = "username";
	private static final String USER_EXISTS_ERROR = "user_exists_error";
	private static final String LOGGED_USER_NOT_EXISTS = "logged_user_not_exists";

	private final UserDetailsServiceImpl userDetailsService;


	@Override
	public boolean supports(Class<?> clazz) {
		return UserDataUpdate.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UserDataUpdate user = (UserDataUpdate) target;

		String username = user.getUsername();
		Optional<UserEntity> foundUser = userDetailsService.findUserByUsername(username);

		String loggedUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<UserEntity> optionalLoggedUser = userDetailsService.findUserByUsername(loggedUserLogin);
		if (optionalLoggedUser.isPresent()) {
			UserEntity loggedUser = optionalLoggedUser.get();
			if (foundUser.isPresent() && !foundUser.get().getId().equals(loggedUser.getId())) {
				errors.rejectValue(USERNAME_FIELD, USER_EXISTS_ERROR, "Użytkownik o takim loginie już istnieje!");
			}
		} else {
			errors.rejectValue(USERNAME_FIELD, LOGGED_USER_NOT_EXISTS, "Zalogowany użytkownik nie istnieje w bazie danych!");
		}
	}
}
