package pl.marcinm312.springquestionsanswers.user.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.mock.mockito.SpyBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import pl.marcinm312.springquestionsanswers.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springquestionsanswers.config.security.SecurityMessagesConfig;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springquestionsanswers.user.model.ActivationTokenEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserCreate;
import pl.marcinm312.springquestionsanswers.user.repository.ActivationTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.MailChangeTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.shared.mail.MailService;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springquestionsanswers.user.service.UserManager;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.ActivationTokenDataProvider;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;
import pl.marcinm312.springquestionsanswers.config.security.utils.SessionUtils;
import pl.marcinm312.springquestionsanswers.user.validator.UserCreateValidator;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
@MockBeans({@MockBean(MailChangeTokenRepo.class)})
@SpyBeans({@SpyBean(UserManager.class), @SpyBean(UserCreateValidator.class), @SpyBean(SessionUtils.class),
		@SpyBean(UserDetailsServiceImpl.class), @SpyBean(PasswordEncoder.class),
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
@WebAppConfiguration
class UserRegistrationWebControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private MailService mailService;

	@MockBean
	private ActivationTokenRepo activationTokenRepo;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private UserRepo userRepo;


	@BeforeEach
	void setup() {

		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.webApplicationContext)
				.apply(springSecurity())
				.alwaysDo(print())
				.build();
	}

	@Test
	void createUserView_simpleCase_success() throws Exception {
		mockMvc.perform(
						get("/register/"))
				.andExpect(status().isOk())
				.andExpect(view().name("register"))
				.andExpect(model().attributeExists("user"))
				.andExpect(unauthenticated());
	}

	@Test
	void createUser_withoutCsrfToken_forbidden() throws Exception {
		UserCreate userToRequest = UserDataProvider.prepareGoodUserToRequest();

		mockMvc.perform(
						post("/register/")
								.param("username", userToRequest.getUsername())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isForbidden());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(activationTokenRepo, never()).save(any(ActivationTokenEntity.class));
		verify(mailService, never()).sendMail(any(String.class), any(String.class),
				any(String.class), eq(true));
	}

	@Test
	void createUser_withInvalidCsrfToken_forbidden() throws Exception {
		UserCreate userToRequest = UserDataProvider.prepareGoodUserToRequest();

		mockMvc.perform(
						post("/register/")
								.with(csrf().useInvalidToken())
								.param("username", userToRequest.getUsername())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isForbidden());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(activationTokenRepo, never()).save(any(ActivationTokenEntity.class));
		verify(mailService, never()).sendMail(any(String.class), any(String.class),
				any(String.class), eq(true));
	}

	@Test
	void createUser_simpleCase_success() throws Exception {
		UserCreate userToRequest = UserDataProvider.prepareGoodUserToRequest();
		UserEntity user = UserEntity.builder()
				.username(userToRequest.getUsername())
				.password(userToRequest.getPassword())
				.email(userToRequest.getEmail())
				.build();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());
		given(activationTokenRepo.save(any(ActivationTokenEntity.class))).willReturn(new ActivationTokenEntity("123456789", user));
		given(userRepo.save(any(UserEntity.class))).willReturn(user);

		mockMvc.perform(
						post("/register/")
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

		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(activationTokenRepo, times(1)).save(any(ActivationTokenEntity.class));
		verify(mailService, times(1)).sendMail(any(String.class), any(String.class),
				any(String.class), eq(true));
	}

	@Test
	void createUser_spacesInPassword_success() throws Exception {
		UserCreate userToRequest = UserDataProvider.prepareUserWithSpacesInPasswordToRequest();
		UserEntity user = UserEntity.builder()
				.username(userToRequest.getUsername())
				.password(userToRequest.getPassword())
				.email(userToRequest.getEmail())
				.build();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());
		given(activationTokenRepo.save(any(ActivationTokenEntity.class))).willReturn(new ActivationTokenEntity("123456789", user));
		given(userRepo.save(any(UserEntity.class))).willReturn(user);

		mockMvc.perform(
						post("/register/")
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

		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(activationTokenRepo, times(1)).save(any(ActivationTokenEntity.class));
		verify(mailService, times(1)).sendMail(any(String.class), any(String.class),
				any(String.class), eq(true));
	}

	@Test
	void createUser_incorrectConfirmPasswordValue_validationError() throws Exception {
		UserCreate userToRequest = UserDataProvider.prepareUserWithConfirmPasswordErrorToRequest();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());

		ModelAndView modelAndView = mockMvc.perform(
						post("/register/")
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("register"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", "confirmPassword"))
				.andExpect(model().attributeExists("user"))
				.andExpect(unauthenticated())
				.andReturn().getModelAndView();

		assert modelAndView != null;
		UserCreate userFromModel = (UserCreate) modelAndView.getModel().get("user");
		Assertions.assertEquals(userToRequest.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(userToRequest.getPassword(), userFromModel.getPassword());
		Assertions.assertEquals(userToRequest.getConfirmPassword(), userFromModel.getConfirmPassword());
		Assertions.assertEquals(userToRequest.getEmail(), userFromModel.getEmail());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(activationTokenRepo, never()).save(any(ActivationTokenEntity.class));
		verify(mailService, never()).sendMail(any(String.class), any(String.class),
				any(String.class), eq(true));
	}

	@Test
	void createUser_userWithTooShortLoginAfterTrim_validationError() throws Exception {
		UserCreate userToRequest = UserDataProvider.prepareUserWithTooShortLoginAfterTrimToRequest();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());

		ModelAndView modelAndView = mockMvc.perform(
						post("/register/")
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("register"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", "username"))
				.andExpect(model().attributeExists("user"))
				.andExpect(unauthenticated())
				.andReturn().getModelAndView();

		assert modelAndView != null;
		UserCreate userFromModel = (UserCreate) modelAndView.getModel().get("user");
		Assertions.assertEquals(userToRequest.getUsername().trim(), userFromModel.getUsername());
		Assertions.assertEquals(userToRequest.getPassword(), userFromModel.getPassword());
		Assertions.assertEquals(userToRequest.getConfirmPassword(), userFromModel.getConfirmPassword());
		Assertions.assertEquals(userToRequest.getEmail(), userFromModel.getEmail());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(activationTokenRepo, never()).save(any(ActivationTokenEntity.class));
		verify(mailService, never()).sendMail(any(String.class), any(String.class),
				any(String.class), eq(true));
	}

	@Test
	void createUser_userAlreadyExists_validationError() throws Exception {
		UserCreate userToRequest = UserDataProvider.prepareGoodUserToRequest();
		UserEntity existingUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.of(existingUser));

		ModelAndView modelAndView = mockMvc.perform(
						post("/register/")
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("register"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", "username"))
				.andExpect(model().attributeExists("user"))
				.andExpect(unauthenticated())
				.andReturn().getModelAndView();

		assert modelAndView != null;
		UserCreate userFromModel = (UserCreate) modelAndView.getModel().get("user");
		Assertions.assertEquals(userToRequest.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(userToRequest.getPassword(), userFromModel.getPassword());
		Assertions.assertEquals(userToRequest.getConfirmPassword(), userFromModel.getConfirmPassword());
		Assertions.assertEquals(userToRequest.getEmail(), userFromModel.getEmail());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(activationTokenRepo, never()).save(any(ActivationTokenEntity.class));
		verify(mailService, never()).sendMail(any(String.class), any(String.class),
				any(String.class), eq(true));
	}

	@Test
	void createUser_incorrectValues_validationError() throws Exception {
		UserCreate userToRequest = UserDataProvider.prepareIncorrectUserToRequest();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());

		ModelAndView modelAndView = mockMvc.perform(
						post("/register/")
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("register"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", "username"))
				.andExpect(model().attributeHasFieldErrors("user", "password"))
				.andExpect(model().attributeHasFieldErrors("user", "confirmPassword"))
				.andExpect(model().attributeHasFieldErrors("user", "email"))
				.andExpect(model().attributeExists("user"))
				.andExpect(unauthenticated())
				.andReturn().getModelAndView();

		assert modelAndView != null;
		UserCreate userFromModel = (UserCreate) modelAndView.getModel().get("user");
		Assertions.assertEquals(userToRequest.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(userToRequest.getPassword(), userFromModel.getPassword());
		Assertions.assertEquals(userToRequest.getConfirmPassword(), userFromModel.getConfirmPassword());
		Assertions.assertEquals(userToRequest.getEmail(), userFromModel.getEmail());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(activationTokenRepo, never()).save(any(ActivationTokenEntity.class));
		verify(mailService, never()).sendMail(any(String.class), any(String.class),
				any(String.class), eq(true));
	}

	@Test
	void createUser_emptyValues_validationError() throws Exception {
		UserCreate userToRequest = UserDataProvider.prepareEmptyUserToRequest();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());

		ModelAndView modelAndView = mockMvc.perform(
						post("/register/")
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("register"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", "username"))
				.andExpect(model().attributeHasFieldErrors("user", "password"))
				.andExpect(model().attributeHasFieldErrors("user", "confirmPassword"))
				.andExpect(model().attributeHasFieldErrors("user", "email"))
				.andExpect(model().attributeExists("user"))
				.andExpect(unauthenticated())
				.andReturn().getModelAndView();

		assert modelAndView != null;
		UserCreate userFromModel = (UserCreate) modelAndView.getModel().get("user");
		Assertions.assertNull(userFromModel.getUsername());
		Assertions.assertEquals(userToRequest.getPassword(), userFromModel.getPassword());
		Assertions.assertEquals(userToRequest.getConfirmPassword(), userFromModel.getConfirmPassword());
		Assertions.assertNull(userFromModel.getEmail());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(activationTokenRepo, never()).save(any(ActivationTokenEntity.class));
		verify(mailService, never()).sendMail(any(String.class), any(String.class),
				any(String.class), eq(true));
	}

	@Test
	void activateUser_simpleCase_userActivated() throws Exception {
		ActivationTokenEntity foundToken = ActivationTokenDataProvider.prepareExampleToken();
		String exampleExistingTokenValue = "123456-123-123-1234";
		given(activationTokenRepo.findByValue(exampleExistingTokenValue)).willReturn(Optional.of(foundToken));
		given(userRepo.save(any(UserEntity.class))).willReturn(foundToken.getUser());

		mockMvc.perform(
						get("/token/?value=" + exampleExistingTokenValue))
				.andExpect(status().isOk())
				.andExpect(view().name("userActivation"))
				.andExpect(unauthenticated());

		verify(activationTokenRepo, times(1)).delete(foundToken);
		verify(userRepo, times(1)).save(any(UserEntity.class));
	}

	@Test
	void activateUser_tokenNotFound_userNotActivated() throws Exception {
		String exampleNotExistingTokenValue = "000-000-000";
		given(activationTokenRepo.findByValue(exampleNotExistingTokenValue)).willReturn(Optional.empty());

		mockMvc.perform(
						get("/token/?value=" + exampleNotExistingTokenValue))
				.andExpect(status().isNotFound())
				.andExpect(view().name("tokenNotFound"))
				.andExpect(unauthenticated());

		verify(activationTokenRepo, never()).delete(any(ActivationTokenEntity.class));
		verify(userRepo, never()).save(any(UserEntity.class));
	}

	@Test
	void activateUser_nullTokenValue_userNotActivated() throws Exception {
		mockMvc.perform(get("/token/"))
				.andExpect(status().isBadRequest())
				.andExpect(unauthenticated());

		verify(activationTokenRepo, never()).delete(any(ActivationTokenEntity.class));
		verify(userRepo, never()).save(any(UserEntity.class));
	}
}
