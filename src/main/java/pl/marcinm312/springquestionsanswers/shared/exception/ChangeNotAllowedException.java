package pl.marcinm312.springquestionsanswers.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ChangeNotAllowedException extends RuntimeException {

	public ChangeNotAllowedException() {
		super("Brak uprawnień do wykonania operacji!");
	}

	public int getHttpStatus() {
		return HttpServletResponse.SC_FORBIDDEN;
	}
}
