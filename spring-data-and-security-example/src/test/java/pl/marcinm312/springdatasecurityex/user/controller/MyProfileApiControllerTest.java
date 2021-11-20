package pl.marcinm312.springdatasecurityex.user.controller;

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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springdatasecurityex.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.config.security.SecurityMessagesConfig;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springdatasecurityex.config.security.utils.SessionUtils;
import pl.marcinm312.springdatasecurityex.shared.mail.MailService;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserDataUpdate;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserGet;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserPasswordUpdate;
import pl.marcinm312.springdatasecurityex.user.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.user.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.user.validator.UserDataUpdateValidator;
import pl.marcinm312.springdatasecurityex.user.validator.UserPasswordUpdateValidator;

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
@MockBeans({@MockBean(TokenRepo.class), @MockBean(MailService.class)})
@SpyBeans({@SpyBean(UserManager.class), @SpyBean(UserDetailsServiceImpl.class),
		@SpyBean(UserDataUpdateValidator.class), @SpyBean(UserPasswordUpdateValidator.class),
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
class MyProfileApiControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private UserRepo userRepo;

	@MockBean
	private SessionUtils sessionUtils;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper mapper = new ObjectMapper();

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

		doNothing().when(userRepo).delete(isA(UserEntity.class));

		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(this.webApplicationContext)
						.apply(springSecurity())
						.alwaysDo(print())
						.build();
	}

	@Test
	void getMyProfile_loggedCommonUser_success() throws Exception {

		String token = prepareToken("user", "password");

		UserEntity expectedUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		String response = mockMvc.perform(
						get("/api/myProfile")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		UserGet responseUser = mapper.readValue(response, UserGet.class);

		assertUser(expectedUser, responseUser);
	}

	@Test
	void getMyProfile_loggedAdminUser_success() throws Exception {

		String token = prepareToken("administrator", "password");

		UserEntity expectedUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();
		String response = mockMvc.perform(
						get("/api/myProfile")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		UserGet responseUser = mapper.readValue(response, UserGet.class);

		assertUser(expectedUser, responseUser);
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
		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateToRequest();
		mockMvc.perform(
				put("/api/myProfile")
						.contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("utf-8")
						.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isUnauthorized());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@ParameterizedTest(name = "{index} ''{3}''")
	@MethodSource("examplesOfGoodProfileUpdates")
	void updateMyProfile_goodUser_success(UserDataUpdate userToRequest, Optional<UserEntity> foundUser,
										  int numberOfExpireSessionInvocations, String nameOfTestCase) throws Exception {

		UserEntity savedUser = new UserEntity(userToRequest.getUsername(), "password", userToRequest.getEmail());
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(foundUser);
		given(userRepo.save(any(UserEntity.class))).willReturn(savedUser);
		given(sessionUtils.expireUserSessions(any(UserEntity.class), eq(true), eq(false))).willReturn(savedUser);

		String token = prepareToken("user", "password");

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
		Assertions.assertEquals(userToRequest.getEmail(), responseUser.getEmail());

		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(sessionUtils, times(numberOfExpireSessionInvocations))
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	private static Stream<Arguments> examplesOfGoodProfileUpdates() {
		UserEntity existingUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		return Stream.of(
				Arguments.of(UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest(), Optional.empty(), 1,
						"updateMyProfile_goodUserWithLoginChange_success"),
				Arguments.of(UserDataProvider.prepareGoodUserDataUpdateToRequest(), Optional.of(existingUser), 0,
						"updateMyProfile_goodUserWithoutLoginChange_success")
		);
	}

	@ParameterizedTest(name = "{index} ''{2}''")
	@MethodSource("examplesOfUpdateMyProfileBadRequests")
	void updateMyProfile_incorrectUser_badRequest(UserDataUpdate userToRequest, Optional<UserEntity> foundUser,
										  String nameOfTestCase) throws Exception {

		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(foundUser);

		String token = prepareToken("user", "password");

		mockMvc.perform(
						put("/api/myProfile")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isBadRequest());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	private static Stream<Arguments> examplesOfUpdateMyProfileBadRequests() {
		UserEntity existingUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		return Stream.of(
				Arguments.of(UserDataProvider.prepareExistingUserDataUpdateToRequest(), Optional.of(existingUser),
						"updateMyProfile_userAlreadyExists_validationError"),
				Arguments.of(UserDataProvider.prepareIncorrectUserDataUpdateToRequest(), Optional.empty(),
						"updateMyProfile_incorrectValues_validationError"),
				Arguments.of(UserDataProvider.prepareUserDataUpdateWithTooShortLoginAfterTrimToRequest(), Optional.empty(),
						"updateMyProfile_userWithTooShortLoginAfterTrim_validationError"),
				Arguments.of(UserDataProvider.prepareEmptyUserDataUpdateToRequest(), Optional.empty(),
						"updateMyProfile_emptyValues_validationError")
		);
	}

	@Test
	void updateMyProfile_nullBody_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(
						put("/api/myProfile")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(""))
				.andExpect(status().isBadRequest());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
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
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyPassword_simpleCase_success() throws Exception {

		UserPasswordUpdate userToRequest = UserDataProvider.prepareGoodUserPasswordUpdateToRequest();
		given(userRepo.save(any(UserEntity.class))).willReturn(commonUser);
		given(sessionUtils.expireUserSessions(any(UserEntity.class), eq(false), eq(false))).willReturn(commonUser);

		String token = prepareToken("user", "password");

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
		Assertions.assertEquals(commonUser.getUsername(), responseUser.getUsername());
		Assertions.assertEquals(commonUser.getEmail(), responseUser.getEmail());

		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(sessionUtils, times(1))
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void updateMyPassword_userWithSpacesInPassword_success() throws Exception {

		UserPasswordUpdate userToRequest = UserDataProvider.prepareUserPasswordUpdateWithSpacesInPassToRequest();
		given(userRepo.save(any(UserEntity.class))).willReturn(userWithSpacesInPass);
		given(sessionUtils.expireUserSessions(any(UserEntity.class), eq(false), eq(false))).willReturn(userWithSpacesInPass);

		String token = prepareToken("user3", " pass ");

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
		Assertions.assertEquals(userWithSpacesInPass.getUsername(), responseUser.getUsername());
		Assertions.assertEquals(userWithSpacesInPass.getEmail(), responseUser.getEmail());

		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(sessionUtils, times(1))
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("examplesOfUpdateMyPasswordBadRequests")
	void updateMyPassword_incorrectData_badRequest(UserPasswordUpdate userToRequest, String nameOfTestCase) throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(
						put("/api/myProfile/updatePassword")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isBadRequest());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	private static Stream<Arguments> examplesOfUpdateMyPasswordBadRequests() {
		return Stream.of(
				Arguments.of(UserDataProvider.prepareUserPasswordUpdateWithIncorrectCurrentPasswordToRequest(),
						"updateMyPassword_incorrectCurrentPassword_validationError"),
				Arguments.of(UserDataProvider.prepareUserPasswordUpdateWithConfirmationErrorToRequest(),
						"updateMyPassword_differentPasswordInConfirmation_validationError"),
				Arguments.of(UserDataProvider.prepareUserPasswordUpdateWithTheSamePasswordAsPreviousToRequest(),
						"updateMyPassword_theSamePasswordAsPrevious_validationError"),
				Arguments.of(UserDataProvider.prepareUserPasswordUpdateWithTooShortPasswordToRequest(),
						"updateMyPassword_tooShortPassword_validationError"),
				Arguments.of(UserDataProvider.prepareEmptyUserPasswordUpdateToRequest(),
						"updateMyPassword_emptyFields_validationError")
		);
	}

	@Test
	void updateMyPassword_nullBody_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(
						put("/api/myProfile/updatePassword")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(""))
				.andExpect(status().isBadRequest());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	private String prepareToken(String username, String password) throws Exception {
		return mockMvc.perform(post("/api/login")
						.content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
						.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(header().exists("Authorization"))
				.andReturn().getResponse().getHeader("Authorization");
	}
}
