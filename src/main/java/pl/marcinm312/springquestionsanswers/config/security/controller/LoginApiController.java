package pl.marcinm312.springquestionsanswers.config.security.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.marcinm312.springquestionsanswers.config.security.model.LoginCredentials;

@SecurityRequirements
@RestController
public class LoginApiController {

	@PostMapping("/api/login")
	public void login(@RequestBody LoginCredentials credentials) {
		// This method is handled by Spring Security. It is added to make the controller method visible in
		// tools such as Swagger
	}
}
