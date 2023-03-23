package pl.marcinm312.springquestionsanswers.user.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TokenNotFoundException extends RuntimeException {

	public TokenNotFoundException() {
		super("Nie znaleziono tokenu!");
		log.error(getMessage());
	}

	public int getHttpStatus() {
		return HttpServletResponse.SC_NOT_FOUND;
	}
}
