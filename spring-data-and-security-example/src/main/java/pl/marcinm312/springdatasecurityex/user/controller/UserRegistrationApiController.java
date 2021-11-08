package pl.marcinm312.springdatasecurityex.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserCreate;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserGet;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;
import pl.marcinm312.springdatasecurityex.user.validator.UserCreateValidator;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class UserRegistrationApiController {

	private final UserManager userManager;
	private final UserCreateValidator userValidator;

	@Autowired
	public UserRegistrationApiController(UserManager userManager, UserCreateValidator userValidator) {
		this.userManager = userManager;
		this.userValidator = userValidator;
	}

	@InitBinder("userCreate")
	private void initBinder(WebDataBinder binder) {
		binder.addValidators(userValidator);
	}

	@PostMapping("/registration")
	public UserGet createUser(@Validated @RequestBody UserCreate user, BindingResult bindingResult,
							  HttpServletResponse response) throws IOException {
		if (bindingResult.hasErrors()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, bindingResult.getFieldErrors().toString());
			return null;
		} else {
			return userManager.addUser(user);
		}
	}
}
