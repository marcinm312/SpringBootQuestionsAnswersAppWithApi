package pl.marcinm312.springdatasecurityex.controller.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.model.Token;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.MailService;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.validator.UserValidator;

import javax.mail.MessagingException;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
class UserRegistrationWebControllerTest {

	private MockMvc mockMvc;

	@Mock
	MailService mailService;

	@Mock
	UserRepo userRepo;

	@Mock
	TokenRepo tokenRepo;

	@Mock
	PasswordEncoder passwordEncoder;

	@InjectMocks
	UserManager userManager;

	@InjectMocks
	UserValidator userValidator;

	@BeforeEach
	void setup() throws MessagingException {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setPrefix("/WEB-INF/view/");
		viewResolver.setSuffix(".jsp");
		doNothing().when(mailService).sendMail(isA(String.class), isA(String.class), isA(String.class), isA(boolean.class));
		given(passwordEncoder.encode(any(CharSequence.class))).willReturn("encodedPassword");
		this.mockMvc = MockMvcBuilders.standaloneSetup(new UserRegistrationWebController(userManager, userValidator))
				.setViewResolvers(viewResolver).alwaysDo(print()).build();
	}

	@Test
	void createUser_simpleCase_success() throws Exception {
		User userToRequest = UserDataProvider.prepareGoodUserToRequest();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());
		given(tokenRepo.save(any(Token.class))).willReturn(new Token(null, "123456789", userToRequest));
		User receivedUser = (User) Objects.requireNonNull(mockMvc.perform(post("/register")
				.param("username", userToRequest.getUsername())
				.param("password", userToRequest.getPassword())
				.param("confirmPassword", userToRequest.getConfirmPassword())
				.param("email", userToRequest.getEmail()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(".."))
				.andExpect(view().name("redirect:.."))
				.andExpect(model().hasNoErrors())
				.andReturn()
				.getModelAndView())
				.getModelMap()
				.getAttribute("user");

		assert receivedUser != null;
		Assertions.assertEquals(userToRequest.getUsername(), receivedUser.getUsername());
		Assertions.assertEquals("encodedPassword", receivedUser.getPassword());
		Assertions.assertFalse(receivedUser.isEnabled());
		Assertions.assertEquals(Roles.ROLE_USER.name(), receivedUser.getRole());
	}

	@Test
	void createUser_incorrectConfirmPasswordValue_validationError() throws Exception {
		User userToRequest = UserDataProvider.prepareUserWithConfirmPasswordErrorToRequest();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());
		mockMvc.perform(post("/register")
				.param("username", userToRequest.getUsername())
				.param("password", userToRequest.getPassword())
				.param("confirmPassword", userToRequest.getConfirmPassword())
				.param("email", userToRequest.getEmail()))
				.andExpect(view().name("register"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", "confirmPassword"));
	}
}