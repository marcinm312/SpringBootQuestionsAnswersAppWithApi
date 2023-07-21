package pl.marcinm312.springquestionsanswers.user.exception;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UserNotExistsException extends RuntimeException {

	public UserNotExistsException() {
		super("Zalogowany użytkownik nie istnieje w bazie danych!");
		log.error(getMessage());
	}

	public int getHttpStatus() {
		return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	}
}
