package pl.marcinm312.springquestionsanswers.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ChangeNotAllowedException extends RuntimeException {

	public ChangeNotAllowedException() {
		super("Brak uprawnień do wykonania operacji!");
		log.error(getMessage());
	}

	public int getHttpStatus() {
		return HttpServletResponse.SC_FORBIDDEN;
	}
}
