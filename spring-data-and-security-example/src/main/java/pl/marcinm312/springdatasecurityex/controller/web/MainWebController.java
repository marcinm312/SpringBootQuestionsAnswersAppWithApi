package pl.marcinm312.springdatasecurityex.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;

@Controller
@RequestMapping("/")
public class MainWebController {

	private UserManager userManager;

	@Autowired
	public MainWebController(UserManager userManager) {
		this.userManager = userManager;
	}

	@GetMapping
	public String getMainPage() {
		return "main";
	}

	@PostMapping("/register")
	public String createUser(@ModelAttribute("user") @Validated User user, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("user", user);
			return "register";
		} else {
			userManager.addUser(user);
			return "redirect:..";
		}
	}

	@GetMapping("/register")
	public String createUserView(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}
}
