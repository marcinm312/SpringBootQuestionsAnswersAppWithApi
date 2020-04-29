package pl.marcinm312.springdatasecurityex.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ChangeNotAllowedException extends RuntimeException {

	public ChangeNotAllowedException(String message) {
		super(message);
	}
}
