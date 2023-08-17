package pl.marcinm312.springquestionsanswers.user.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserDataUpdate;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;

@RequiredArgsConstructor
@Component
public class UserDataUpdateValidator implements Validator {

	private static final String USERNAME_FIELD = "username";
	private static final String USER_EXISTS_ERROR = "user_exists_error";

	private final UserDetailsServiceImpl userDetailsService;


	@Override
	public boolean supports(Class<?> clazz) {
		return UserDataUpdate.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		UserDataUpdate userRequest = (UserDataUpdate) target;

		String newUsername = userRequest.getUsername();
		String loggedUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();

		if (!loggedUserLogin.equals(newUsername) && userDetailsService.findUserByUsername(newUsername).isPresent()) {
			errors.rejectValue(USERNAME_FIELD, USER_EXISTS_ERROR, "Użytkownik o takim loginie już istnieje!");
		}
	}
}
