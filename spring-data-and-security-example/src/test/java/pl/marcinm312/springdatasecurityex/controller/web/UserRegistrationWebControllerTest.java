package pl.marcinm312.springdatasecurityex.controller.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.mock.mockito.SpyBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import pl.marcinm312.springdatasecurityex.config.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.model.user.Token;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.MailService;
import pl.marcinm312.springdatasecurityex.utils.SessionUtils;
import pl.marcinm312.springdatasecurityex.service.db.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.validator.UserValidator;

import javax.mail.MessagingException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserRegistrationWebController.class)
@ComponentScan(basePackageClasses = UserRegistrationWebController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = UserRegistrationWebController.class)
		})
@SpyBeans({@SpyBean(UserManager.class), @SpyBean(UserValidator.class), @SpyBean(SessionUtils.class), @SpyBean(UserDetailsServiceImpl.class)})
@Import({MultiHttpSecurityCustomConfig.class})
@WebAppConfiguration
class UserRegistrationWebControllerTest {

	private MockMvc mockMvc;

	@MockBean
	MailService mailService;

	@MockBean
	TokenRepo tokenRepo;

	@MockBean
	PasswordEncoder passwordEncoder;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	UserRepo userRepo;

	@BeforeEach
	void setup() throws MessagingException {
		doNothing().when(mailService).sendMail(isA(String.class), isA(String.class), isA(String.class), isA(boolean.class));
		given(passwordEncoder.encode(any(CharSequence.class))).willReturn("encodedPassword");

		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.webApplicationContext)
				.apply(springSecurity())
				.alwaysDo(print())
				.build();
	}

	@Test
	@WithAnonymousUser
	void createUser_withoutCsrfToken_forbidden() throws Exception {
		User userToRequest = UserDataProvider.prepareGoodUserToRequest();

		mockMvc.perform(
				post("/register")
						.param("username", userToRequest.getUsername())
						.param("password", userToRequest.getPassword())
						.param("confirmPassword", userToRequest.getConfirmPassword())
						.param("email", userToRequest.getEmail()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithAnonymousUser
	void createUser_withInvalidCsrfToken_forbidden() throws Exception {
		User userToRequest = UserDataProvider.prepareGoodUserToRequest();

		mockMvc.perform(
				post("/register")
						.with(csrf().useInvalidToken())
						.param("username", userToRequest.getUsername())
						.param("password", userToRequest.getPassword())
						.param("confirmPassword", userToRequest.getConfirmPassword())
						.param("email", userToRequest.getEmail()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithAnonymousUser
	void createUser_simpleCase_success() throws Exception {
		User userToRequest = UserDataProvider.prepareGoodUserToRequest();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());
		given(tokenRepo.save(any(Token.class))).willReturn(new Token(null, "123456789", userToRequest));

		mockMvc.perform(
				post("/register")
						.with(csrf())
						.param("username", userToRequest.getUsername())
						.param("password", userToRequest.getPassword())
						.param("confirmPassword", userToRequest.getConfirmPassword())
						.param("email", userToRequest.getEmail()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(".."))
				.andExpect(view().name("redirect:.."))
				.andExpect(model().hasNoErrors())
				.andExpect(unauthenticated());
	}

	@Test
	@WithAnonymousUser
	void createUser_incorrectConfirmPasswordValue_validationError() throws Exception {
		User userToRequest = UserDataProvider.prepareUserWithConfirmPasswordErrorToRequest();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());

		ModelAndView modelAndView = mockMvc.perform(
				post("/register")
						.with(csrf())
						.param("username", userToRequest.getUsername())
						.param("password", userToRequest.getPassword())
						.param("confirmPassword", userToRequest.getConfirmPassword())
						.param("email", userToRequest.getEmail()))
				.andExpect(view().name("register"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", "confirmPassword"))
				.andExpect(model().attributeExists("user"))
				.andExpect(unauthenticated())
				.andReturn().getModelAndView();

		assert modelAndView != null;
		User userFromModel = (User) modelAndView.getModel().get("user");
		Assertions.assertEquals(userToRequest.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(userToRequest.getPassword(), userFromModel.getPassword());
		Assertions.assertFalse(userFromModel.isEnabled());
	}
}