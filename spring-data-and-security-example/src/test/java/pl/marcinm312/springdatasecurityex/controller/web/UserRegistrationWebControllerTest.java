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
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import pl.marcinm312.springdatasecurityex.config.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.model.Token;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.MailService;
import pl.marcinm312.springdatasecurityex.service.SessionUtils;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.validator.UserValidator;

import javax.mail.MessagingException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserRegistrationWebController.class)
@ContextConfiguration(classes = UserRegistrationWebController.class)
@SpyBeans({@SpyBean(UserManager.class), @SpyBean(UserValidator.class), @SpyBean(SessionUtils.class)})
@Import({MultiHttpSecurityCustomConfig.class})
@WebAppConfiguration
class UserRegistrationWebControllerTest {

	private MockMvc mockMvc;

	@MockBean
	MailService mailService;

	@MockBean
	UserRepo userRepo;

	@MockBean
	TokenRepo tokenRepo;

	@MockBean
	PasswordEncoder passwordEncoder;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private static final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";


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
	void createUser_simpleCase_success() throws Exception {
		User userToRequest = UserDataProvider.prepareGoodUserToRequest();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());
		given(tokenRepo.save(any(Token.class))).willReturn(new Token(null, "123456789", userToRequest));

		HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
		CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

		mockMvc.perform(
				post("/register")
						.sessionAttr(TOKEN_ATTR_NAME, csrfToken)
						.param(csrfToken.getParameterName(), csrfToken.getToken())
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

		HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
		CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

		ModelAndView modelAndView = mockMvc.perform(
				post("/register")
						.sessionAttr(TOKEN_ATTR_NAME, csrfToken)
						.param(csrfToken.getParameterName(), csrfToken.getToken())
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