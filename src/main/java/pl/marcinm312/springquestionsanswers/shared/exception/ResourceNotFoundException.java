package pl.marcinm312.springquestionsanswers.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
		log.error(getMessage());
	}

	public int getHttpStatus() {
		return HttpServletResponse.SC_NOT_FOUND;
	}
}
