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
import pl.marcinm312.springdatasecurityex.validator.PasswordUpdateValidator;
import pl.marcinm312.springdatasecurityex.validator.UserValidator;

@Controller
@RequestMapping("/app/myProfile")
public class MyProfileWebController {

	private final UserManager userManager;
	private final UserValidator userValidator;
	private final PasswordUpdateValidator passwordUpdateValidator;
	private final SessionUtils sessionUtils;

	@Autowired
	public MyProfileWebController(UserManager userManager, UserValidator userValidator,
			PasswordUpdateValidator passwordUpdateValidator, SessionUtils sessionUtils) {
		this.userManager = userManager;
		this.userValidator = userValidator;
		this.passwordUpdateValidator = passwordUpdateValidator;
		this.sessionUtils = sessionUtils;
	}

	@InitBinder("user")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(userValidator);
	}

	@InitBinder("user2")
	protected void initBinder2(WebDataBinder binder) {
		binder.addValidators(passwordUpdateValidator);
	}

	@GetMapping
	public String myProfileView(Model model, Authentication authentication) {
		String userName = authentication.getName();
		User user = userManager.getUserByAuthentication(authentication);
		model.addAttribute("userLogin", userName);
		model.addAttribute("user", user);
		return "myProfile";
	}

	@PostMapping("/update")
	public String updateMyProfile(@ModelAttribute("user") @Validated User user, BindingResult bindingResult,
			Model model, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			model.addAttribute("userLogin", userName);
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
		model.addAttribute("userLogin", userName);
		model.addAttribute("user", user);
		return "updateMyProfile";
	}

	@PostMapping("/updatePassword")
	public String updateMyPassword(@ModelAttribute("user2") @Validated User user, BindingResult bindingResult,
			Model model, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			model.addAttribute("userLogin", userName);
			model.addAttribute("user2", user);
			return "updateMyPassword";
		} else {
			userManager.updateUserPassword(user, authentication);
			return "redirect:..";
		}
	}

	@GetMapping("/updatePassword")
	public String updateMyPasswordView(Model model, Authentication authentication) {
		String userName = authentication.getName();
		User user = userManager.getUserByAuthentication(authentication);
		model.addAttribute("userLogin", userName);
		model.addAttribute("user2", user);
		return "updateMyPassword";
	}

	@GetMapping("/endOtherSessions")
	public String endOtherSessions(Authentication authentication) {
		String userName = authentication.getName();
		sessionUtils.expireUserSessions(userName, false);
		return "redirect:..";
	}

	@PostMapping("/delete")
	public String deleteUser(Authentication authentication) {
		userManager.deleteUser(authentication);
		return "redirect:../../..";
	}

	@GetMapping("/delete")
	public String deleteUserConfirmation(Model model, Authentication authentication) {
		String userName = authentication.getName();
		model.addAttribute("userLogin", userName);
		model.addAttribute("user", userManager.getUserByAuthentication(authentication));
		return "deleteMyProfile";
	}
}
