package pl.marcinm312.springdatasecurityex.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserDataUpdate;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserPasswordUpdate;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.utils.SessionUtils;
import pl.marcinm312.springdatasecurityex.validator.UserDataUpdateValidator;
import pl.marcinm312.springdatasecurityex.validator.UserPasswordUpdateValidator;

@Controller
@RequestMapping("/app/myProfile")
public class MyProfileWebController {

	private static final String USER_LOGIN = "userLogin";
	private static final String USER = "user";
	private static final String MY_PROFILE_VIEW = "myProfile";
	private static final String UPDATE_MY_PROFILE_VIEW = "updateMyProfile";
	private static final String USER_2 = "user2";
	private static final String USER_3 = "user3";
	private static final String UPDATE_MY_PASSWORD_VIEW = "updateMyPassword";
	private static final String DELETE_MY_PROFILE_VIEW = "deleteMyProfile";
	private static final String COMMON_REDIRECT = "redirect:..";

	private final UserManager userManager;
	private final UserDataUpdateValidator userDataUpdateValidator;
	private final UserPasswordUpdateValidator userPasswordUpdateValidator;
	private final SessionUtils sessionUtils;

	@Autowired
	public MyProfileWebController(UserManager userManager, UserDataUpdateValidator userDataUpdateValidator,
								  UserPasswordUpdateValidator userPasswordUpdateValidator, SessionUtils sessionUtils) {
		this.userManager = userManager;
		this.userDataUpdateValidator = userDataUpdateValidator;
		this.userPasswordUpdateValidator = userPasswordUpdateValidator;
		this.sessionUtils = sessionUtils;
	}

	@InitBinder("user")
	private void initBinder(WebDataBinder binder) {
		binder.addValidators(userDataUpdateValidator);
	}

	@InitBinder("user2")
	private void initBinder2(WebDataBinder binder) {
		binder.addValidators(userPasswordUpdateValidator);
	}

	@GetMapping
	public String myProfileView(Model model, Authentication authentication) {
		String userName = authentication.getName();
		User user = userManager.getUserByAuthentication(authentication);
		model.addAttribute(USER_LOGIN, userName);
		model.addAttribute(USER_3, user);
		return MY_PROFILE_VIEW;
	}

	@PostMapping("/update")
	public String updateMyProfile(@ModelAttribute("user") @Validated UserDataUpdate user, BindingResult bindingResult,
								  Model model, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			model.addAttribute(USER_LOGIN, userName);
			model.addAttribute(USER, user);
			return UPDATE_MY_PROFILE_VIEW;
		} else {
			userManager.updateUserData(user, authentication);
			return COMMON_REDIRECT;
		}
	}

	@GetMapping("/update")
	public String updateMyProfileView(Model model, Authentication authentication) {
		String userName = authentication.getName();
		User user = userManager.getUserByAuthentication(authentication);
		UserDataUpdate userDataUpdate = new UserDataUpdate(user.getUsername(), user.getEmail());
		model.addAttribute(USER_LOGIN, userName);
		model.addAttribute(USER, userDataUpdate);
		return UPDATE_MY_PROFILE_VIEW;
	}

	@PostMapping("/updatePassword")
	public String updateMyPassword(@ModelAttribute("user2") @Validated UserPasswordUpdate user, BindingResult bindingResult,
								   Model model, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			model.addAttribute(USER_LOGIN, userName);
			model.addAttribute(USER_2, user);
			return UPDATE_MY_PASSWORD_VIEW;
		} else {
			userManager.updateUserPassword(user, authentication);
			return COMMON_REDIRECT;
		}
	}

	@GetMapping("/updatePassword")
	public String updateMyPasswordView(Model model, Authentication authentication) {
		String userName = authentication.getName();
		model.addAttribute(USER_LOGIN, userName);
		model.addAttribute(USER_2, new UserPasswordUpdate());
		return UPDATE_MY_PASSWORD_VIEW;
	}

	@GetMapping("/endOtherSessions")
	public String endOtherSessions(Authentication authentication) {
		String userName = authentication.getName();
		sessionUtils.expireUserSessions(userName, false);
		return COMMON_REDIRECT;
	}

	@PostMapping("/delete")
	public String deleteUser(Authentication authentication) {
		userManager.deleteUser(authentication);
		return "redirect:../../..";
	}

	@GetMapping("/delete")
	public String deleteUserConfirmation(Model model, Authentication authentication) {
		String userName = authentication.getName();
		model.addAttribute(USER_LOGIN, userName);
		model.addAttribute(USER_3, userManager.getUserByAuthentication(authentication));
		return DELETE_MY_PROFILE_VIEW;
	}
}
