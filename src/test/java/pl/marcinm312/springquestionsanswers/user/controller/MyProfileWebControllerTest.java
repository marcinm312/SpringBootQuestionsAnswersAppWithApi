package pl.marcinm312.springquestionsanswers.user.controller;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import pl.marcinm312.springquestionsanswers.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springquestionsanswers.config.security.SecurityMessagesConfig;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springquestionsanswers.mail.service.MailSendService;
import pl.marcinm312.springquestionsanswers.user.model.MailChangeTokenEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserDataUpdate;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserPasswordUpdate;
import pl.marcinm312.springquestionsanswers.user.repository.ActivationTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.MailChangeTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springquestionsanswers.user.service.UserManager;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.MailChangeTokenDataProvider;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;
import pl.marcinm312.springquestionsanswers.config.security.utils.SessionUtils;
import pl.marcinm312.springquestionsanswers.user.validator.UserDataUpdateValidator;
import pl.marcinm312.springquestionsanswers.user.validator.UserPasswordUpdateValidator;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MyProfileWebController.class)
@ComponentScan(basePackageClasses = MyProfileWebController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = MyProfileWebController.class)
		})
@MockBeans({@MockBean(ActivationTokenRepo.class)})
@SpyBeans({@SpyBean(UserManager.class), @SpyBean(UserDetailsServiceImpl.class),
		@SpyBean(UserDataUpdateValidator.class), @SpyBean(UserPasswordUpdateValidator.class),
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
class MyProfileWebControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private UserRepo userRepo;

	@MockBean
	private MailChangeTokenRepo mailChangeTokenRepo;

	@MockBean
	private MailSendService mailSendService;

	@MockBean
	private SessionUtils sessionUtils;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();
	private final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final UserEntity userWithSpacesInPass = UserDataProvider.prepareExampleGoodUserWithEncodedPasswordWithSpaces();

	@BeforeEach
	void setup() {

		given(userRepo.findByUsername("user")).willReturn(Optional.of(commonUser));
		given(userRepo.findByUsername("admin")).willReturn(Optional.of(adminUser));
		given(userRepo.findByUsername("user3")).willReturn(Optional.of(userWithSpacesInPass));

		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(this.webApplicationContext)
						.apply(springSecurity())
						.alwaysDo(print())
						.build();
	}

	@Test
	void myProfileView_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/myProfile/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@Test
	void myProfileView_loggedCommonUser_success() throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/myProfile/")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("myProfile"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		UserEntity userFromModel = (UserEntity) modelAndView.getModel().get("user3");
		UserEntity expectedUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		Assertions.assertEquals(expectedUser.getId(), userFromModel.getId());
		Assertions.assertEquals(expectedUser.getCreatedAt(), userFromModel.getCreatedAt());
		Assertions.assertEquals(expectedUser.getUpdatedAt(), userFromModel.getUpdatedAt());
		Assertions.assertEquals(expectedUser.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(expectedUser.getRole(), userFromModel.getRole());
		Assertions.assertEquals(expectedUser.getEmail(), userFromModel.getEmail());
		Assertions.assertEquals(expectedUser.isEnabled(), userFromModel.isEnabled());
	}

	@Test
	void updateMyProfileView_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/myProfile/update/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@Test
	void updateMyProfileView_loggedCommonUser_success() throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/myProfile/update/")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("updateMyProfile"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		UserDataUpdate userFromModel = (UserDataUpdate) modelAndView.getModel().get("user");
		UserEntity expectedUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		Assertions.assertEquals(expectedUser.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(expectedUser.getEmail(), userFromModel.getEmail());
	}

	@Test
	void updateMyProfile_withAnonymousUser_redirectToLoginPage() throws Exception {

		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest();
		mockMvc.perform(
						post("/app/myProfile/update/")
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyProfile_withoutCsrfToken_forbidden() throws Exception {

		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest();
		mockMvc.perform(
						post("/app/myProfile/update/")
								.with(user("user").password("password"))
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isForbidden());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyProfile_withCsrfInvalidToken_forbidden() throws Exception {

		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest();
		mockMvc.perform(
						post("/app/myProfile/update/")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken())
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isForbidden());

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

		mockMvc.perform(
						post("/app/myProfile/update/")
								.with(user("user").password("password"))
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(".."))
				.andExpect(view().name("redirect:.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(sessionUtils, times(numberOfExpireSessionInvocations))
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
		verify(mailSendService, times(numberOfSendEmailInvocations)).sendMailAsync(eq(commonUser.getEmail()), any(String.class),
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
	void updateMyProfile_incorrectUser_validationError(UserDataUpdate userToRequest, UserEntity existingUser,
														   String expectedLogin, String[] errorFields) throws Exception {

		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.ofNullable(existingUser));

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/myProfile/update/")
								.with(user("user").password("password"))
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("updateMyProfile"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", errorFields))
				.andExpect(model().attributeExists("user", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		UserDataUpdate userFromModel = (UserDataUpdate) modelAndView.getModel().get("user");
		Assertions.assertEquals(expectedLogin, userFromModel.getUsername());
		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	private static Stream<Arguments> examplesOfUpdateMyProfileBadRequests() {

		UserEntity existingUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();

		UserDataUpdate existingUserRequest = UserDataProvider.prepareExistingUserDataUpdateToRequest();
		UserDataUpdate incorrectUserRequest = UserDataProvider.prepareIncorrectUserDataUpdateToRequest();
		UserDataUpdate tooShortLoginAfterTrimUserRequest = UserDataProvider.prepareUserDataUpdateWithTooShortLoginAfterTrimToRequest();
		UserDataUpdate emptyUserRequest = UserDataProvider.prepareEmptyUserDataUpdateToRequest();

		return Stream.of(
				Arguments.of(existingUserRequest, existingUser, existingUserRequest.getUsername(),
						new String[]{"username"}),
				Arguments.of(incorrectUserRequest, null, incorrectUserRequest.getUsername(),
						new String[]{"username", "email"}),
				Arguments.of(tooShortLoginAfterTrimUserRequest, null, tooShortLoginAfterTrimUserRequest.getUsername().trim(),
						new String[]{"username"}),
				Arguments.of(emptyUserRequest, null, null,
						new String[]{"username", "email"})
		);
	}

	@Test
	void updateMyPasswordView_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/myProfile/updatePassword/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@Test
	void updateMyPasswordView_loggedCommonUser_success() throws Exception {

		mockMvc.perform(
						get("/app/myProfile/updatePassword/")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("updateMyPassword"))
				.andExpect(model().attributeExists("userLogin", "user2"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	void updateMyPassword_withAnonymousUser_redirectToLoginPage() throws Exception {

		UserPasswordUpdate userToRequest = UserDataProvider.prepareGoodUserPasswordUpdateToRequest();
		mockMvc.perform(
						post("/app/myProfile/updatePassword/")
								.with(csrf())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void updateMyPassword_withoutCsrfToken_forbidden() throws Exception {

		UserPasswordUpdate userToRequest = UserDataProvider.prepareGoodUserPasswordUpdateToRequest();
		mockMvc.perform(
						post("/app/myProfile/updatePassword/")
								.with(user("user").password("password"))
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(status().isForbidden());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void updateMyPassword_withCsrfInvalidToken_forbidden() throws Exception {

		UserPasswordUpdate userToRequest = UserDataProvider.prepareGoodUserPasswordUpdateToRequest();
		mockMvc.perform(
						post("/app/myProfile/updatePassword/")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(status().isForbidden());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@ParameterizedTest
	@MethodSource("examplesOfUpdateMyPasswordGoodRequests")
	void updateMyPassword_goodRequest_success(UserEntity loggedUser, String password, UserPasswordUpdate userToRequest)
			throws Exception {

		given(userRepo.save(any(UserEntity.class))).willReturn(loggedUser);
		given(sessionUtils.expireUserSessions(any(UserEntity.class), eq(false), eq(false))).willReturn(loggedUser);

		mockMvc.perform(
						post("/app/myProfile/updatePassword/")
								.with(user(loggedUser.getUsername()).password(password))
								.with(csrf())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(".."))
				.andExpect(view().name("redirect:.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername(loggedUser.getUsername()).withRoles("USER"));

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
	void updateMyPassword_incorrectData_validationErrors(UserPasswordUpdate userToRequest, String[] errorFields)
			throws Exception {

		mockMvc.perform(
						post("/app/myProfile/updatePassword/")
								.with(user("user").password("password"))
								.with(csrf())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("updateMyPassword"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user2", errorFields))
				.andExpect(model().attributeExists("userLogin", "user2"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	private static Stream<Arguments> examplesOfUpdateMyPasswordBadRequests() {

		return Stream.of(
				Arguments.of(UserDataProvider.prepareUserPasswordUpdateWithIncorrectCurrentPasswordToRequest(),
						new String[]{"currentPassword"}),
				Arguments.of(UserDataProvider.prepareUserPasswordUpdateWithConfirmationErrorToRequest(),
						new String[]{"confirmPassword"}),
				Arguments.of(UserDataProvider.prepareUserPasswordUpdateWithTheSamePasswordAsPreviousToRequest(),
						new String[]{"password"}),
				Arguments.of(UserDataProvider.prepareUserPasswordUpdateWithTooShortPasswordToRequest(),
						new String[]{"password", "confirmPassword"}),
				Arguments.of(UserDataProvider.prepareEmptyUserPasswordUpdateToRequest(),
						new String[]{"currentPassword", "password", "confirmPassword"})
		);
	}

	@Test
	void expireOtherSessions_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/myProfile/expireOtherSessions/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void expireOtherSessions_simpleCase_success() throws Exception {

		given(userRepo.save(any(UserEntity.class))).willReturn(commonUser);
		given(sessionUtils.expireUserSessions(any(UserEntity.class), eq(false), eq(false))).willReturn(commonUser);

		mockMvc.perform(
						get("/app/myProfile/expireOtherSessions/")
								.with(user("user").password("password")))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:.."))
				.andExpect(redirectedUrl(".."))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(sessionUtils, times(1))
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void deleteMyProfileConfirmation_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/myProfile/delete/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@Test
	void deleteMyProfileConfirmation_loggedCommonUser_success() throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/myProfile/delete/")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("deleteMyProfile"))
				.andExpect(model().attributeExists("userLogin", "user3"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		UserEntity userFromModel = (UserEntity) modelAndView.getModel().get("user3");
		UserEntity expectedUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		Assertions.assertEquals(expectedUser.getId(), userFromModel.getId());
		Assertions.assertEquals(expectedUser.getCreatedAt(), userFromModel.getCreatedAt());
		Assertions.assertEquals(expectedUser.getUpdatedAt(), userFromModel.getUpdatedAt());
		Assertions.assertEquals(expectedUser.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(expectedUser.getRole(), userFromModel.getRole());
		Assertions.assertEquals(expectedUser.getEmail(), userFromModel.getEmail());
		Assertions.assertEquals(expectedUser.isEnabled(), userFromModel.isEnabled());
	}

	@Test
	void deleteMyProfile_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						post("/app/myProfile/delete/")
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());

		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(true));
		verify(userRepo, never()).delete(any(UserEntity.class));
	}

	@Test
	void deleteMyProfile_withoutCsrfToken_forbidden() throws Exception {

		mockMvc.perform(
						post("/app/myProfile/delete/")
								.with(user("user").password("password")))
				.andExpect(status().isForbidden());

		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(true));
		verify(userRepo, never()).delete(any(UserEntity.class));
	}

	@Test
	void deleteMyProfile_withCsrfInvalidToken_forbidden() throws Exception {

		mockMvc.perform(
						post("/app/myProfile/delete/")
								.with(csrf().useInvalidToken())
								.with(user("user").password("password")))
				.andExpect(status().isForbidden());

		verify(sessionUtils, never()).expireUserSessions(any(UserEntity.class), eq(true), eq(true));
		verify(userRepo, never()).delete(any(UserEntity.class));
	}

	@Test
	void deleteMyProfile_simpleCase_success() throws Exception {

		mockMvc.perform(
						post("/app/myProfile/delete/")
								.with(user("user").password("password"))
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:../../.."))
				.andExpect(redirectedUrl("../../.."))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(sessionUtils, times(1))
				.expireUserSessions(any(UserEntity.class), eq(true), eq(true));
		verify(userRepo, times(1)).delete(any(UserEntity.class));
	}

	@Test
	void confirmMailChange_withAnonymousUser_redirectToLoginPage() throws Exception {

		String exampleTokenValue = "123456-123-123-1234";
		mockMvc.perform(
						get("/app/myProfile/update/confirm/?value=" + exampleTokenValue))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());

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

		mockMvc.perform(
						get("/app/myProfile/update/confirm/?value=" + exampleExistingTokenValue)
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("confirmMailChange"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(model().attribute("newEmail", foundToken.getNewEmail()))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(mailChangeTokenRepo, times(1)).deleteByUser(foundToken.getUser());
		verify(userRepo, times(1)).save(any(UserEntity.class));
	}

	@Test
	void confirmMailChange_tokenNotFound_changeNotConfirmed() throws Exception {

		String exampleNotExistingTokenValue = "000-000-000";
		given(mailChangeTokenRepo.findByValueAndUsername(exampleNotExistingTokenValue, "user"))
				.willReturn(Optional.empty());

		mockMvc.perform(
						get("/app/myProfile/update/confirm/?value=" + exampleNotExistingTokenValue)
								.with(user("user").password("password")))
				.andExpect(status().isNotFound())
				.andExpect(view().name("confirmTokenNotFound"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(mailChangeTokenRepo, never()).deleteByUser(any(UserEntity.class));
		verify(userRepo, never()).save(any(UserEntity.class));
	}

	@Test
	void confirmMailChange_nullTokenValue_changeNotConfirmed() throws Exception {

		mockMvc.perform(get("/app/myProfile/update/confirm/?value=")
						.with(user("user").password("password")))
				.andExpect(status().isBadRequest())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(mailChangeTokenRepo, never()).deleteByUser(any(UserEntity.class));
		verify(userRepo, never()).save(any(UserEntity.class));
	}
}
