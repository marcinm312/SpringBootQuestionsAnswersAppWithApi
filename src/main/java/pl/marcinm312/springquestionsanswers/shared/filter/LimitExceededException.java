package pl.marcinm312.springquestionsanswers.shared.filter;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LimitExceededException extends RuntimeException {

	public LimitExceededException(int limit) {
		super(String.format("Strona nie może zawierać więcej niż %d rekordów", limit));
	}

	public int getHttpStatus() {
		return HttpServletResponse.SC_BAD_REQUEST;
	}
}
