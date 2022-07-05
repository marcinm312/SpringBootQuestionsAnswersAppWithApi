package pl.marcinm312.springdatasecurityex.user.controller;

import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/myProfile")
public class MyProfileApiController {

	private final UserManager userManager;
	private final UserDataUpdateValidator userDataUpdateValidator;
	private final UserPasswordUpdateValidator userPasswordUpdateValidator;


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
