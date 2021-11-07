package pl.marcinm312.springdatasecurityex.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserGet;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;

@RestController
@RequestMapping("/api/myProfile")
public class MyProfileApiController {

	private final UserManager userManager;

	@Autowired
	public MyProfileApiController(UserManager userManager) {
		this.userManager = userManager;
	}

	@GetMapping
	public UserGet getMyProfile(Authentication authentication) {
		return userManager.getUserDTOByAuthentication(authentication);
	}
}
