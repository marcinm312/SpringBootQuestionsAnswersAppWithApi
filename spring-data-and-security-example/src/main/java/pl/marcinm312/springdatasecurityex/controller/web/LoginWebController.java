package pl.marcinm312.springdatasecurityex.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginWebController {

	@GetMapping("/showLoginPage")
	public String showLoginPage() {
		return "loginForm";
	}
}
