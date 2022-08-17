package pl.marcinm312.springdatasecurityex.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TokenNotFoundException extends RuntimeException {

	public TokenNotFoundException() {
		super("Nie znaleziono tokenu!");
	}

	public int getHttpStatus() {
		return HttpServletResponse.SC_NOT_FOUND;
	}
}
