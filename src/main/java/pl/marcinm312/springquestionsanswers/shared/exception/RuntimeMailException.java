package pl.marcinm312.springquestionsanswers.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class RuntimeMailException extends RuntimeException {

	public RuntimeMailException(String message) {
		super(message);
	}
}