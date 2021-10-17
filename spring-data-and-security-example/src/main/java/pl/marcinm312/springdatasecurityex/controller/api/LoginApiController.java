package pl.marcinm312.springdatasecurityex.controller.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.marcinm312.springdatasecurityex.model.credentials.LoginCredentials;

@RestController
public class LoginApiController {

	@PostMapping("/api/login")
	public void login(@RequestBody LoginCredentials credentials) {
		// This method is handled by Spring Security. It is added to make the controller method visible in
		// tools such as Swagger
	}
}
