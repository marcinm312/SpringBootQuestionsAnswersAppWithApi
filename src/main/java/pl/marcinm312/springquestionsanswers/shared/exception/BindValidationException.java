package pl.marcinm312.springquestionsanswers.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BindValidationException extends BindException {

	public BindValidationException(BindingResult bindingResult) {
		super(bindingResult);
	}
}
