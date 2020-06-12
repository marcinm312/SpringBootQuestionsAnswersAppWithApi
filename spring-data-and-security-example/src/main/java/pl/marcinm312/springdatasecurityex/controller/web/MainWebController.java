package pl.marcinm312.springdatasecurityex.controller.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import pl.marcinm312.springdatasecurityex.exception.TokenNotFoundException;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.validator.UserValidator;

@Controller
@RequestMapping("/")
public class MainWebController {

	private final UserManager userManager;
	private final UserValidator userValidator;

	@Autowired
	public MainWebController(UserManager userManager, UserValidator userValidator) {
		this.userManager = userManager;
		this.userValidator = userValidator;
	}

	@InitBinder("user")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(userValidator);
	}

	@GetMapping
	public String getMainPage() {
		return "main";
	}

	@PostMapping("/register")
	public String createUser(@ModelAttribute("user") @Validated User user, BindingResult bindingResult, Model model,
			HttpServletRequest request) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("user", user);
			return "register";
		} else {
			String requestURL = request.getRequestURL().toString();
			String servletPath = request.getServletPath();
			String appURL = requestURL.replace(servletPath, "");
			userManager.addUser(user, false, appURL);
			return "redirect:..";
		}
	}

	@GetMapping("/register")
	public String createUserView(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	@GetMapping("/token")
	public String activateUser(@RequestParam String value) {
		try {
			userManager.activateUser(value);
		} catch (TokenNotFoundException e) {
			return "tokenNotFound";
		}
		return "userActivation";
	}
}
