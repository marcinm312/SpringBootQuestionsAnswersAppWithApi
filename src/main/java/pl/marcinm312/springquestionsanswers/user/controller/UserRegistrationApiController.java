package pl.marcinm312.springquestionsanswers.user.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserCreate;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserGet;
import pl.marcinm312.springquestionsanswers.user.service.UserManager;
import pl.marcinm312.springquestionsanswers.user.validator.UserCreateValidator;

@SecurityRequirements
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserRegistrationApiController {

	private final UserManager userManager;
	private final UserCreateValidator userValidator;


	@InitBinder("userCreate")
	private void initBinder(WebDataBinder binder) {
		binder.addValidators(userValidator);
	}

	@PostMapping("/registration")
	public UserGet createUser(@Validated @RequestBody UserCreate user, BindingResult bindingResult) throws BindException {

		if (bindingResult.hasErrors()) {
			throw new BindException(bindingResult);
		}
		return userManager.addUser(user);
	}

	@PutMapping("/token")
	public UserGet activateUser(@RequestParam String value) {
		return userManager.activateUser(value);
	}
}
