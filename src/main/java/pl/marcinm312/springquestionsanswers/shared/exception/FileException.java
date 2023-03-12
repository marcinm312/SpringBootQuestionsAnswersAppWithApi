package pl.marcinm312.springquestionsanswers.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileException extends Exception {

	public FileException(String message) {
		super(message);
		log.error(getMessage());
	}
}
