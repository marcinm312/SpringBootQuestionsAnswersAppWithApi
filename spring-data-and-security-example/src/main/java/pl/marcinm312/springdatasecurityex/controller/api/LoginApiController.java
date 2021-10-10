package pl.marcinm312.springdatasecurityex.controller.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.marcinm312.springdatasecurityex.model.credentials.LoginCredentials;

@RestController
public class LoginApiController {

	@PostMapping("/api/login")
	public void login(@RequestBody LoginCredentials credentials) {

	}
}
