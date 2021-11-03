package pl.marcinm312.springdatasecurityex.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ChangeNotAllowedException extends RuntimeException {

	public ChangeNotAllowedException() {
		super("Change not allowed!");
	}
}
