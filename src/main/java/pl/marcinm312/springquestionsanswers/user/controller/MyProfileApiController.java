package pl.marcinm312.springquestionsanswers.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserMapper;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserDataUpdate;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserGet;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserPasswordUpdate;
import pl.marcinm312.springquestionsanswers.user.service.UserManager;
import pl.marcinm312.springquestionsanswers.user.validator.UserDataUpdateValidator;
import pl.marcinm312.springquestionsanswers.user.validator.UserPasswordUpdateValidator;

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

		UserEntity user = userManager.getUserFromDB(authentication);
		return UserMapper.convertUserToUserGet(user, false);
	}

	@PutMapping
	public UserGet updateMyProfile(@Validated @RequestBody UserDataUpdate user, BindingResult bindingResult,
								   Authentication authentication) throws BindException {

		if (bindingResult.hasErrors()) {
			throw new BindException(bindingResult);
		}
		return userManager.updateUserData(user, authentication);
	}

	@PutMapping("/confirmMailChange")
	public UserGet confirmMailChange(@RequestParam String value, Authentication authentication) {
		return userManager.confirmMailChange(value, authentication);
	}

	@PutMapping("/updatePassword")
	public UserGet updateMyPassword(@Validated @RequestBody UserPasswordUpdate user, BindingResult bindingResult,
									Authentication authentication) throws BindException {

		if (bindingResult.hasErrors()) {
			throw new BindException(bindingResult);
		}
		return userManager.updateUserPassword(user, authentication);
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
