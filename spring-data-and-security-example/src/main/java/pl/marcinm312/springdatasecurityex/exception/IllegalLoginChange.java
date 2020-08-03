package pl.marcinm312.springdatasecurityex.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class IllegalLoginChange extends RuntimeException {

    public IllegalLoginChange() {
        super("Illegal login change!");
    }
}
