package pl.marcinm312.springdatasecurityex.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;

@Controller
@RequestMapping("/app/myprofile")
public class MyProfileWebController {

	private UserManager userManager;

	@Autowired
	public MyProfileWebController(UserManager userManager) {
		this.userManager = userManager;
	}

	@GetMapping
	public String myProfileView(Model model, Authentication authentication) {
		String userName = authentication.getName();
		User user = userManager.getUserByAuthentication(authentication);
		model.addAttribute("userlogin", userName);
		model.addAttribute("user", user);
		return "myProfile";
	}
}
