package pl.marcinm312.springdatasecurityex.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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

import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.service.SessionUtils;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.validator.UserValidator;

@Controller
@RequestMapping("/app/myprofile")
public class MyProfileWebController {

	private UserManager userManager;
	private UserValidator userValidator;
	private SessionUtils sessionUtils;

	@Autowired
	public MyProfileWebController(UserManager userManager, UserValidator userValidator, SessionUtils sessionUtils) {
		this.userManager = userManager;
		this.userValidator = userValidator;
		this.sessionUtils = sessionUtils;
	}

	@InitBinder("user")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(userValidator);
	}

	@GetMapping
	public String myProfileView(Model model, Authentication authentication) {
		String userName = authentication.getName();
		User user = userManager.getUserByAuthentication(authentication);
		model.addAttribute("userlogin", userName);
		model.addAttribute("user", user);
		return "myProfile";
	}

	@PostMapping("/update")
	public String updateMyProfile(@ModelAttribute("user") @Validated User user, BindingResult bindingResult,
			Model model, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			model.addAttribute("userlogin", userName);
			model.addAttribute("user", user);
			return "updateMyProfile";
		} else {
			userManager.updateUserData(user, authentication);
			return "redirect:..";
		}
	}

	@GetMapping("/update")
	public String updateMyProfileView(Model model, Authentication authentication) {
		String userName = authentication.getName();
		User user = userManager.getUserByAuthentication(authentication);
		model.addAttribute("userlogin", userName);
		model.addAttribute("user", user);
		return "updateMyProfile";
	}

	@GetMapping("/endOtherSessions")
	public String endOtherSessions(Authentication authentication) {
		String userName = authentication.getName();
		sessionUtils.expireUserSessionsExceptTheCurrentOne(userName);
		return "redirect:..";
	}
}
