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
import pl.marcinm312.springdatasecurityex.user.model.dto.UserPasswordUpdate;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;
import pl.marcinm312.springdatasecurityex.user.validator.UserDataUpdateValidator;
import pl.marcinm312.springdatasecurityex.user.validator.UserPasswordUpdateValidator;

@RestController
@RequestMapping("/api/myProfile")
public class MyProfileApiController {

	private final UserManager userManager;
	private final UserDataUpdateValidator userDataUpdateValidator;
	private final UserPasswordUpdateValidator userPasswordUpdateValidator;

	@Autowired
	public MyProfileApiController(UserManager userManager, UserDataUpdateValidator userDataUpdateValidator,
								  UserPasswordUpdateValidator userPasswordUpdateValidator) {
		this.userManager = userManager;
		this.userDataUpdateValidator = userDataUpdateValidator;
		this.userPasswordUpdateValidator = userPasswordUpdateValidator;
	}

	@InitBinder("userDataUpdate")
	private void initUserDataUpdateBinder(WebDataBinder binder) {
		binder.addValidators(userDataUpdateValidator);
	}

	@InitBinder("userPasswordUpdate")
	private void initUserPasswordUpdateBinder(WebDataBinder binder) {
		binder.addValidators(userPasswordUpdateValidator);
	}

	@GetMapping
	public UserGet getMyProfile(Authentication authentication) {
		return userManager.getUserDTOByAuthentication(authentication);
	}

	@PutMapping
	public UserGet updateMyProfile(@Validated @RequestBody UserDataUpdate user, BindingResult bindingResult,
								   Authentication authentication) throws BindException {
		if (bindingResult.hasErrors()) {
			throw new BindException(bindingResult);
		} else {
			return userManager.updateUserData(user, authentication);
		}
	}

	@PutMapping("/updatePassword")
	public UserGet updateMyPassword(@Validated @RequestBody UserPasswordUpdate user, BindingResult bindingResult,
									Authentication authentication) throws BindException {
		if (bindingResult.hasErrors()) {
			throw new BindException(bindingResult);
		} else {
			return userManager.updateUserPassword(user, authentication);
		}
	}

	@DeleteMapping
	public boolean deleteMyProfile(Authentication authentication) {
		return userManager.deleteUser(authentication);
	}

	@PutMapping("/expireOtherSessions")
	public UserGet expireOtherSessions(Authentication authentication) {
		return userManager.expireOtherSessions(authentication);
	}
}
