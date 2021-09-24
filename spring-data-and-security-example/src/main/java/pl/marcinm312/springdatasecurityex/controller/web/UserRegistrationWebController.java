package pl.marcinm312.springdatasecurityex.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.exception.TokenNotFoundException;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserCreate;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.validator.UserCreateValidator;

@Controller
@RequestMapping("/")
public class UserRegistrationWebController {

    private static final String USER = "user";
    private static final String REGISTER_VIEW = "register";
    private static final String TOKEN_NOT_FOUND_VIEW = "tokenNotFound";
    private static final String USER_ACTIVATION_VIEW = "userActivation";
    
    private final UserManager userManager;
    private final UserCreateValidator userValidator;

    @Autowired
    public UserRegistrationWebController(UserManager userManager, UserCreateValidator userValidator) {
        this.userManager = userManager;
        this.userValidator = userValidator;
    }

    @InitBinder("user")
    private void initBinder(WebDataBinder binder) {
        binder.addValidators(userValidator);
    }

    @PostMapping("/register")
    public String createUser(@ModelAttribute("user") @Validated UserCreate user, BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(USER, user);
            return REGISTER_VIEW;
        } else {
            userManager.addUser(user);
            return "redirect:..";
        }
    }

    @GetMapping("/register")
    public String createUserView(Model model) {
        model.addAttribute(USER, new UserCreate());
        return REGISTER_VIEW;
    }

    @GetMapping("/token")
    public String activateUser(@RequestParam String value) {
        try {
            userManager.activateUser(value);
        } catch (TokenNotFoundException e) {
            return TOKEN_NOT_FOUND_VIEW;
        }
        return USER_ACTIVATION_VIEW;
    }
}
