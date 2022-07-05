package pl.marcinm312.springdatasecurityex.config.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginWebController {

	@GetMapping("/loginPage")
	public String showLoginPage() {
		return "loginForm";
	}
}
