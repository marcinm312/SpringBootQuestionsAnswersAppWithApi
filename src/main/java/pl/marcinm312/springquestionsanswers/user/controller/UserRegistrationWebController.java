package pl.marcinm312.springquestionsanswers.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springquestionsanswers.user.exception.TokenNotFoundException;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserCreate;
import pl.marcinm312.springquestionsanswers.user.service.UserManager;
import pl.marcinm312.springquestionsanswers.user.validator.UserCreateValidator;

import jakarta.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Controller
@RequestMapping("/")
public class UserRegistrationWebController {

	private static final String USER = "user";
	private static final String REGISTER_VIEW = "register";
	private static final String TOKEN_NOT_FOUND_VIEW = "tokenNotFound";
	private static final String USER_ACTIVATION_VIEW = "userActivation";

	private final UserManager userManager;
	private final UserCreateValidator userValidator;


	@InitBinder("user")
	private void initBinder(WebDataBinder binder) {
		binder.addValidators(userValidator);
	}

	@PostMapping("/register")
	public String createUser(@ModelAttribute("user") @Validated UserCreate user, BindingResult bindingResult,
							 Model model, HttpServletResponse response) {

		if (bindingResult.hasErrors()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			model.addAttribute(USER, user);
			return REGISTER_VIEW;
		}
		userManager.addUser(user);
		return "redirect:..";
	}

	@GetMapping("/register")
	public String createUserView(Model model) {

		model.addAttribute(USER, new UserCreate());
		return REGISTER_VIEW;
	}

	@GetMapping("/token")
	public String activateUser(@RequestParam String value, HttpServletResponse response) {

		try {
			userManager.activateUser(value);
		} catch (TokenNotFoundException e) {
			response.setStatus(e.getHttpStatus());
			return TOKEN_NOT_FOUND_VIEW;
		}
		return USER_ACTIVATION_VIEW;
	}
}
