package pl.marcinm312.springdatasecurityex.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserDataUpdate;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserGet;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;
import pl.marcinm312.springdatasecurityex.user.validator.UserDataUpdateValidator;

@RestController
@RequestMapping("/api/myProfile")
public class MyProfileApiController {

	private final UserManager userManager;
	private final UserDataUpdateValidator userDataUpdateValidator;

	@Autowired
	public MyProfileApiController(UserManager userManager, UserDataUpdateValidator userDataUpdateValidator) {
		this.userManager = userManager;
		this.userDataUpdateValidator = userDataUpdateValidator;
	}

	@InitBinder("userDataUpdate")
	private void initBinder(WebDataBinder binder) {
		binder.addValidators(userDataUpdateValidator);
	}

	@GetMapping
	public UserGet getMyProfile(Authentication authentication) {
		return userManager.getUserDTOByAuthentication(authentication);
	}

	@PutMapping("/update")
	public UserGet updateMyProfile(@Validated @RequestBody UserDataUpdate user, BindingResult bindingResult,
								   Authentication authentication) throws BindException {
		if (bindingResult.hasErrors()) {
			throw new BindException(bindingResult);
		} else {
			return userManager.updateUserData(user, authentication);
		}
	}
}
