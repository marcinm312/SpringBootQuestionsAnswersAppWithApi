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
import org.springframework.web.bind.annotation.RequestParam;

import pl.marcinm312.springdatasecurityex.model.Token;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;

@Controller
@RequestMapping("/")
public class MainWebController {

	private UserManager userManager;
	private TokenRepo tokenRepo;
	private UserRepo userRepo;

	@Autowired
	public MainWebController(UserManager userManager, TokenRepo tokenRepo, UserRepo userRepo) {
		this.userManager = userManager;
		this.tokenRepo = tokenRepo;
		this.userRepo = userRepo;
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
			userManager.addUser(user, false);
			return "redirect:..";
		}
	}

	@GetMapping("/register")
	public String createUserView(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	@GetMapping("/token")
	public String activateUser(@RequestParam String value) {
		Token token = tokenRepo.findByValue(value);
		User user = token.getUser();
		user.setEnabled(true);
		userRepo.save(user);
		return "userActivation";
	}
}
