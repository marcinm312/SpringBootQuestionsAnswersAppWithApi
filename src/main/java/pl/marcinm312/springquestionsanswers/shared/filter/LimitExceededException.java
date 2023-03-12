package pl.marcinm312.springquestionsanswers.shared.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LimitExceededException extends RuntimeException {

	public LimitExceededException(int limit) {
		super(String.format("Strona nie może zawierać więcej niż %d rekordów", limit));
		log.error(getMessage());
	}

	public int getHttpStatus() {
		return HttpServletResponse.SC_BAD_REQUEST;
	}
}
