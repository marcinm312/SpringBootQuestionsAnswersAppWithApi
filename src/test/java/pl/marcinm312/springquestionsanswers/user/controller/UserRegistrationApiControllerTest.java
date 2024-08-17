package pl.marcinm312.springquestionsanswers.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.mock.mockito.SpyBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springquestionsanswers.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springquestionsanswers.config.security.SecurityMessagesConfig;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springquestionsanswers.config.security.utils.SessionUtils;
import pl.marcinm312.springquestionsanswers.mail.service.MailSender;
import pl.marcinm312.springquestionsanswers.user.model.ActivationTokenEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserCreate;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserGet;
import pl.marcinm312.springquestionsanswers.user.repository.ActivationTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.MailChangeTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springquestionsanswers.user.service.UserManager;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.ActivationTokenDataProvider;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;
import pl.marcinm312.springquestionsanswers.user.validator.UserCreateValidator;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserRegistrationApiController.class)
@ComponentScan(basePackageClasses = UserRegistrationApiController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UserRegistrationApiController.class)
		})
@MockBeans({@MockBean(MailChangeTokenRepo.class)})
@SpyBeans({@SpyBean(UserManager.class), @SpyBean(UserCreateValidator.class), @SpyBean(SessionUtils.class),
		@SpyBean(UserDetailsServiceImpl.class), @SpyBean(PasswordEncoder.class),
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
@WebAppConfiguration
class UserRegistrationApiControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private MailSender mailSender;

	@MockBean
	private UserRepo userRepo;

	@MockBean
	private ActivationTokenRepo activationTokenRepo;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper mapper = new ObjectMapper();

	@BeforeEach
	void setup() {

		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.webApplicationContext)
				.apply(springSecurity())
				.alwaysDo(print())
				.build();
	}

	@ParameterizedTest
	@MethodSource("examplesOfUserRegistrationGoodRequests")
	void createUser_goodUser_success(UserCreate userToRequest) throws Exception {

		UserEntity user = UserEntity.builder()
				.username(userToRequest.getUsername())
				.password(userToRequest.getPassword())
				.email(userToRequest.getEmail())
				.build();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());
		given(activationTokenRepo.save(any(ActivationTokenEntity.class))).willReturn(new ActivationTokenEntity("123456789", user));
		given(userRepo.save(any(UserEntity.class))).willReturn(user);

		String response = mockMvc.perform(
						post("/api/registration")
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		UserGet responseUser = mapper.readValue(response, UserGet.class);
		Assertions.assertEquals(userToRequest.getUsername(), responseUser.getUsername());
		Assertions.assertEquals(userToRequest.getEmail(), responseUser.getEmail());
		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(mailSender, times(1)).sendMail(any(String.class), any(String.class),
				any(String.class), eq(true));
	}

	private static Stream<Arguments> examplesOfUserRegistrationGoodRequests() {

		return Stream.of(
				Arguments.of(UserDataProvider.prepareGoodUserToRequest()),
				Arguments.of(UserDataProvider.prepareUserWithSpacesInPasswordToRequest()),
				Arguments.of(UserDataProvider.prepareGoodUserWithActivationUrlToRequest()),
				Arguments.of(UserDataProvider.prepareGoodUserWithIncorrectActivationUrlToRequest())
		);
	}

	@ParameterizedTest
	@MethodSource("examplesOfUserRegistrationBadRequests")
	void createUser_incorrectUser_validationError(UserCreate userToRequest, UserEntity foundUserWithTheSameLogin)
			throws Exception {

		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.ofNullable(foundUserWithTheSameLogin));

		mockMvc.perform(
						post("/api/registration")
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isBadRequest());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(activationTokenRepo, never()).save(any(ActivationTokenEntity.class));
		verify(mailSender, never()).sendMail(any(String.class), any(String.class), any(String.class), eq(true));
	}

	private static Stream<Arguments> examplesOfUserRegistrationBadRequests() {

		UserEntity existingUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		return Stream.of(
				Arguments.of(UserDataProvider.prepareUserWithConfirmPasswordErrorToRequest(), null),
				Arguments.of(UserDataProvider.prepareUserWithTooShortLoginAfterTrimToRequest(), null),
				Arguments.of(UserDataProvider.prepareGoodUserToRequest(), existingUser),
				Arguments.of(UserDataProvider.prepareIncorrectUserToRequest(), null),
				Arguments.of(UserDataProvider.prepareEmptyUserToRequest(), null)
		);
	}

	@Test
	void createUser_nullBody_validationError() throws Exception {

		mockMvc.perform(
						post("/api/registration")
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(""))
				.andExpect(status().isBadRequest());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(activationTokenRepo, never()).save(any(ActivationTokenEntity.class));
		verify(mailSender, never()).sendMail(any(String.class), any(String.class), any(String.class), eq(true));
	}

	@Test
	void activateUser_simpleCase_userActivated() throws Exception {

		ActivationTokenEntity foundToken = ActivationTokenDataProvider.prepareExampleToken();
		String exampleExistingTokenValue = "123456-123-123-1234";
		given(activationTokenRepo.findByValue(exampleExistingTokenValue)).willReturn(Optional.of(foundToken));
		given(userRepo.save(any(UserEntity.class))).willReturn(foundToken.getUser());

		mockMvc.perform(
						put("/api/token?value=" + exampleExistingTokenValue))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(unauthenticated());

		verify(activationTokenRepo, times(1)).delete(foundToken);
		verify(userRepo, times(1)).save(any(UserEntity.class));
	}

	@Test
	void activateUser_tokenNotFound_userNotActivated() throws Exception {

		String exampleNotExistingTokenValue = "000-000-000";
		given(activationTokenRepo.findByValue(exampleNotExistingTokenValue)).willReturn(Optional.empty());

		mockMvc.perform(
						put("/api/token?value=" + exampleNotExistingTokenValue))
				.andExpect(status().isNotFound())
				.andExpect(unauthenticated());

		verify(activationTokenRepo, never()).delete(any(ActivationTokenEntity.class));
		verify(userRepo, never()).save(any(UserEntity.class));
	}

	@Test
	void activateUser_nullTokenValue_userNotActivated() throws Exception {
		
		mockMvc.perform(put("/api/token"))
				.andExpect(status().isBadRequest())
				.andExpect(unauthenticated());

		verify(activationTokenRepo, never()).delete(any(ActivationTokenEntity.class));
		verify(userRepo, never()).save(any(UserEntity.class));
	}
}
