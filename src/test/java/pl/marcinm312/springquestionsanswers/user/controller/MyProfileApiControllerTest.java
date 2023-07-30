package pl.marcinm312.springquestionsanswers.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springquestionsanswers.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springquestionsanswers.config.security.SecurityMessagesConfig;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springquestionsanswers.config.security.utils.SessionUtils;
import pl.marcinm312.springquestionsanswers.shared.mail.MailService;
import pl.marcinm312.springquestionsanswers.shared.testdataprovider.JwtProvider;
import pl.marcinm312.springquestionsanswers.user.model.MailChangeTokenEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserDataUpdate;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserGet;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserPasswordUpdate;
import pl.marcinm312.springquestionsanswers.user.repository.ActivationTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.MailChangeTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springquestionsanswers.user.service.UserManager;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.MailChangeTokenDataProvider;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;
import pl.marcinm312.springquestionsanswers.user.validator.UserDataUpdateValidator;
import pl.marcinm312.springquestionsanswers.user.validator.UserPasswordUpdateValidator;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MyProfileApiController.class)
@ComponentScan(basePackageClasses = MyProfileApiController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = MyProfileApiController.class)
		})
@MockBeans({@MockBean(ActivationTokenRepo.class)})
@SpyBeans({@SpyBean(UserManager.class), @SpyBean(UserDetailsServiceImpl.class),
		@SpyBean(UserDataUpdateValidator.class), @SpyBean(UserPasswordUpdateValidator.class),
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
class MyProfileApiControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private UserRepo userRepo;

	@MockBean
	private MailChangeTokenRepo mailChangeTokenRepo;

	@MockBean
	private MailService mailService;

	@MockBean
	private SessionUtils sessionUtils;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

	private final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();
	private final UserEntity userWithSpacesInPass = UserDataProvider.prepareExampleGoodUserWithEncodedPasswordWithSpaces();

	@BeforeEach
	void setup() {

		given(userRepo.findById(commonUser.getId())).willReturn(Optional.of(commonUser));
		given(userRepo.findById(adminUser.getId())).willReturn(Optional.of(adminUser));
		given(userRepo.findById(userWithSpacesInPass.getId())).willReturn(Optional.of(userWithSpacesInPass));

		given(userRepo.findByUsername(commonUser.getUsername())).willReturn(Optional.of(commonUser));
		given(userRepo.findByUsername(adminUser.getUsername())).willReturn(Optional.of(adminUser));
		given(userRepo.findByUsername(userWithSpacesInPass.getUsername())).willReturn(Optional.of(userWithSpacesInPass));

		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(this.webApplicationContext)
						.apply(springSecurity())
						.alwaysDo(print())
						.build();
	}

	@ParameterizedTest
	@MethodSource("examplesOfGetMyProfileSuccess")
	void getMyProfile_adminOrCommonUser_success(UserEntity loggedUser) throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken(loggedUser.getUsername(), "password");
		String response = mockMvc.perform(
						get("/api/myProfile")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		UserGet responseUser = mapper.readValue(response, UserGet.class);
		assertUser(loggedUser, responseUser);
	}

	private static Stream<Arguments> examplesOfGetMyProfileSuccess() {

		return Stream.of(
			Arguments.of(UserDataProvider.prepareExampleGoodUserWithEncodedPassword()),
			Arguments.of(UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword())
		);
	}

	@Test
	void getMyProfile_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(
						get("/api/myProfile"))
				.andExpect(status().isUnauthorized());
	}

	private void assertUser(UserEntity expectedUser, UserGet responseUser) {

		Assertions.assertEquals(expectedUser.getId(), responseUser.getId());
		Assertions.assertEquals(expectedUser.getCreatedAt(), responseUser.getCreatedAt());
		Assertions.assertEquals(expectedUser.getUpdatedAt(), responseUser.getUpdatedAt());
		Assertions.assertEquals(expectedUser.getUsername(), responseUser.getUsername());
		Assertions.assertEquals(expectedUser.getRole(), responseUser.getRole());
		Assertions.assertEquals(expectedUser.getEmail(), responseUser.getEmail());
		Assertions.assertEquals(expectedUser.isEnabled(), responseUser.isEnabled());
	}

	@Test
	void updateMyProfile_withAnonymousUser_unauthorized() throws Exception {

		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateWithoutChangesToRequest();
		mockMvc.perform(
						put("/api/myProfile")
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isUnauthorized());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@ParameterizedTest
	@MethodSource("examplesOfGoodProfileUpdates")
	void updateMyProfile_goodUser_success(UserDataUpdate userToRequest, UserEntity foundUserWithTheSameLogin,
										  int numberOfExpireSessionInvocations, int numberOfSendEmailInvocations)
			throws Exception {

		UserEntity savedUser = UserEntity.builder()
				.username(userToRequest.getUsername())
				.password(commonUser.getPassword())
				.email(commonUser.getEmail())
				.build();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.ofNullable(foundUserWithTheSameLogin));
		given(userRepo.save(any(UserEntity.class))).willReturn(savedUser);
		given(sessionUtils.expireUserSessions(any(UserEntity.class), eq(true), eq(false))).willReturn(savedUser);

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		String response = mockMvc.perform(
						put("/api/myProfile")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		UserGet responseUser = mapper.readValue(response, UserGet.class);
		Assertions.assertEquals(userToRequest.getUsername(), responseUser.getUsername());
		Assertions.assertEquals(commonUser.getEmail(), responseUser.getEmail());

		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(sessionUtils, times(numberOfExpireSessionInvocations))
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
		verify(mailService, times(numberOfSendEmailInvocations)).sendMail(any(String.class), any(String.class),
				any(String.class), eq(true));
	}

	private static Stream<Arguments> examplesOfGoodProfileUpdates() {

		UserEntity existingUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		return Stream.of(
				Arguments.of(UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest(), null, 1, 0),
				Arguments.of(UserDataProvider.prepareGoodUserDataUpdateWithoutChangesToRequest(), existingUser, 0, 0),
				Arguments.of(UserDataProvider.prepareGoodUserDataUpdateWithEmailChangeToRequest(), existingUser, 0, 1),
				Arguments.of(UserDataProvider.prepareGoodUserDataUpdateWithLoginAndEmailChangeToRequest(), null, 1, 1)
		);
	}

	@ParameterizedTest
	@MethodSource("examplesOfUpdateMyProfileBadRequests")
	void updateMyProfile_incorrectUser_badRequest(UserDataUpdate userToRequest, UserEntity foundUser)
			throws Exception {

		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.ofNullable(foundUser));

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(
						put("/api/myProfile")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isBadRequest());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	private static Stream<Arguments> examplesOfUpdateMyProfileBadRequests() {

		UserEntity existingUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		return Stream.of(
				Arguments.of(UserDataProvider.prepareExistingUserDataUpdateToRequest(), existingUser),
				Arguments.of(UserDataProvider.prepareIncorrectUserDataUpdateToRequest(), null),
				Arguments.of(UserDataProvider.prepareUserDataUpdateWithTooShortLoginAfterTrimToRequest(), null),
				Arguments.of(UserDataProvider.prepareEmptyUserDataUpdateToRequest(), null)
		);
	}

	@Test
	void updateMyProfile_nullBody_badRequest() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(
						put("/api/myProfile")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(""))
				.andExpect(status().isBadRequest());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyPassword_withAnonymousUser_unauthorized() throws Exception {

		UserPasswordUpdate userToRequest = UserDataProvider.prepareGoodUserPasswordUpdateToRequest();
		mockMvc.perform(
						put("/api/myProfile/updatePassword")
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isUnauthorized());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@ParameterizedTest
	@MethodSource("examplesOfUpdateMyPasswordGoodRequests")
	void updateMyPassword_goodRequest_success(UserEntity loggedUser, String password, UserPasswordUpdate userToRequest)
			throws Exception {

		given(userRepo.save(any(UserEntity.class))).willReturn(loggedUser);
		given(sessionUtils.expireUserSessions(any(UserEntity.class), eq(false), eq(false))).willReturn(loggedUser);

		String token = new JwtProvider(mockMvc).prepareToken(loggedUser.getUsername(), password);
		String response = mockMvc.perform(
						put("/api/myProfile/updatePassword")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		UserGet responseUser = mapper.readValue(response, UserGet.class);
		Assertions.assertEquals(loggedUser.getUsername(), responseUser.getUsername());
		Assertions.assertEquals(loggedUser.getEmail(), responseUser.getEmail());
		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(sessionUtils, times(1))
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	private static Stream<Arguments> examplesOfUpdateMyPasswordGoodRequests() {

		return Stream.of(
			Arguments.of(UserDataProvider.prepareExampleGoodUserWithEncodedPassword(), "password",
					UserDataProvider.prepareGoodUserPasswordUpdateToRequest()),
			Arguments.of(UserDataProvider.prepareExampleGoodUserWithEncodedPasswordWithSpaces(), " pas ",
					UserDataProvider.prepareUserPasswordUpdateWithSpacesInPassToRequest())
		);
	}

	@ParameterizedTest
	@MethodSource("examplesOfUpdateMyPasswordBadRequests")
	void updateMyPassword_incorrectData_badRequest(UserPasswordUpdate userToRequest) throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(
						put("/api/myProfile/updatePassword")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isBadRequest());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	private static Stream<Arguments> examplesOfUpdateMyPasswordBadRequests() {

		return Stream.of(
				Arguments.of(UserDataProvider.prepareUserPasswordUpdateWithIncorrectCurrentPasswordToRequest()),
				Arguments.of(UserDataProvider.prepareUserPasswordUpdateWithConfirmationErrorToRequest()),
				Arguments.of(UserDataProvider.prepareUserPasswordUpdateWithTheSamePasswordAsPreviousToRequest()),
				Arguments.of(UserDataProvider.prepareUserPasswordUpdateWithTooShortPasswordToRequest()),
				Arguments.of(UserDataProvider.prepareEmptyUserPasswordUpdateToRequest())
		);
	}

	@Test
	void updateMyPassword_nullBody_badRequest() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(
						put("/api/myProfile/updatePassword")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(""))
				.andExpect(status().isBadRequest());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void deleteMyProfile_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(
						delete("/api/myProfile"))
				.andExpect(status().isUnauthorized());

		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(true));
		verify(userRepo, never()).delete(any(UserEntity.class));
	}

	@Test
	void deleteMyProfile_simpleCase_success() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		String response = mockMvc.perform(
						delete("/api/myProfile")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Assertions.assertEquals("true", response);
		verify(sessionUtils, times(1))
				.expireUserSessions(any(UserEntity.class), eq(true), eq(true));
		verify(userRepo, times(1)).delete(any(UserEntity.class));
	}

	@Test
	void expireOtherSessions_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(
						put("/api/myProfile/expireOtherSessions"))
				.andExpect(status().isUnauthorized());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void expireOtherSessions_simpleCase_success() throws Exception {

		given(userRepo.save(any(UserEntity.class))).willReturn(commonUser);
		given(sessionUtils.expireUserSessions(any(UserEntity.class), eq(false), eq(false))).willReturn(commonUser);

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		String response = mockMvc.perform(
						put("/api/myProfile/expireOtherSessions")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		UserGet responseUser = mapper.readValue(response, UserGet.class);
		Assertions.assertEquals(commonUser.getUsername(), responseUser.getUsername());
		Assertions.assertEquals(commonUser.getEmail(), responseUser.getEmail());
		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(sessionUtils, times(1))
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void confirmMailChange_withAnonymousUser_unauthorized() throws Exception {

		String exampleTokenValue = "123456-123-123-1234";
		mockMvc.perform(
						put("/api/myProfile/confirmMailChange?value=" + exampleTokenValue))
				.andExpect(status().isUnauthorized());

		verify(mailChangeTokenRepo, never()).deleteByUser(any(UserEntity.class));
		verify(userRepo, never()).save(any(UserEntity.class));
	}

	@Test
	void confirmMailChange_simpleCase_changeConfirmed() throws Exception {

		MailChangeTokenEntity foundToken = MailChangeTokenDataProvider.prepareExampleToken();
		String exampleExistingTokenValue = "123456-123-123-1234";
		given(mailChangeTokenRepo.findByValueAndUsername(exampleExistingTokenValue, "user"))
				.willReturn(Optional.of(foundToken));
		given(userRepo.save(any(UserEntity.class))).willReturn(foundToken.getUser());

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(
						put("/api/myProfile/confirmMailChange?value=" + exampleExistingTokenValue)
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));

		verify(mailChangeTokenRepo, times(1)).deleteByUser(foundToken.getUser());
		verify(userRepo, times(1)).save(any(UserEntity.class));
	}

	@Test
	void confirmMailChange_tokenNotFound_changeNotConfirmed() throws Exception {

		String exampleNotExistingTokenValue = "000-000-000";
		given(mailChangeTokenRepo.findByValueAndUsername(exampleNotExistingTokenValue, "user"))
				.willReturn(Optional.empty());

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(
						put("/api/myProfile/confirmMailChange?value=" + exampleNotExistingTokenValue)
								.header("Authorization", token))
				.andExpect(status().isNotFound());

		verify(mailChangeTokenRepo, never()).deleteByUser(any(UserEntity.class));
		verify(userRepo, never()).save(any(UserEntity.class));
	}

	@Test
	void activateUser_nullTokenValue_userNotActivated() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(put("/api/myProfile/confirmMailChange?value=")
						.header("Authorization", token))
				.andExpect(status().isBadRequest());

		verify(mailChangeTokenRepo, never()).deleteByUser(any(UserEntity.class));
		verify(userRepo, never()).save(any(UserEntity.class));
	}
}
