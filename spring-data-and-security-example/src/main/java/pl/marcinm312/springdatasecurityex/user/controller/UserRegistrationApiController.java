package pl.marcinm312.springdatasecurityex.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserCreate;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserGet;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;
import pl.marcinm312.springdatasecurityex.user.validator.UserCreateValidator;

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
	public UserGet createUser(@Validated @RequestBody UserCreate user, BindingResult bindingResult) throws BindException {
		if (bindingResult.hasErrors()) {
			throw new BindException(bindingResult);
		} else {
			return userManager.addUser(user);
		}
	}
}
