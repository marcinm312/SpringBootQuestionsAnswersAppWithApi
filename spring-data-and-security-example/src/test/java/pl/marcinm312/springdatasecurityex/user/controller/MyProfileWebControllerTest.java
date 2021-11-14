package pl.marcinm312.springdatasecurityex.user.controller;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import pl.marcinm312.springdatasecurityex.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.config.security.SecurityMessagesConfig;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserDataUpdate;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserPasswordUpdate;
import pl.marcinm312.springdatasecurityex.user.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.user.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.shared.mail.MailService;
import pl.marcinm312.springdatasecurityex.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.config.security.utils.SessionUtils;
import pl.marcinm312.springdatasecurityex.user.validator.UserDataUpdateValidator;
import pl.marcinm312.springdatasecurityex.user.validator.UserPasswordUpdateValidator;

import java.util.Optional;

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
@MockBeans({@MockBean(TokenRepo.class), @MockBean(MailService.class)})
@SpyBeans({@SpyBean(UserManager.class), @SpyBean(UserDetailsServiceImpl.class),
		@SpyBean(UserDataUpdateValidator.class), @SpyBean(UserPasswordUpdateValidator.class),
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
class MyProfileWebControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private UserRepo userRepo;

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
		given(userRepo.findByUsername("administrator")).willReturn(Optional.of(adminUser));
		given(userRepo.findByUsername("user3")).willReturn(Optional.of(userWithSpacesInPass));

		doNothing().when(userRepo).delete(isA(UserEntity.class));

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
						get("/app/myProfile"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());
	}

	@Test
	void myProfileView_loggedCommonUser_success() throws Exception {
		UserEntity expectedUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/myProfile")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("myProfile"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		UserEntity userFromModel = (UserEntity) modelAndView.getModel().get("user3");
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
						get("/app/myProfile/update"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());
	}

	@Test
	void updateMyProfileView_loggedCommonUser_success() throws Exception {
		UserEntity expectedUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/myProfile/update")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("updateMyProfile"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		UserDataUpdate userFromModel = (UserDataUpdate) modelAndView.getModel().get("user");
		Assertions.assertEquals(expectedUser.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(expectedUser.getEmail(), userFromModel.getEmail());
	}

	@Test
	void updateMyProfile_withAnonymousUser_redirectToLoginPage() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest();

		mockMvc.perform(
						post("/app/myProfile/update")
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyProfile_withoutCsrfToken_forbidden() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest();

		mockMvc.perform(
						post("/app/myProfile/update")
								.with(user("user").password("password"))
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isForbidden());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyProfile_withCsrfInvalidToken_forbidden() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest();

		mockMvc.perform(
						post("/app/myProfile/update")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken())
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isForbidden());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyProfile_goodUserWithLoginChange_success() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest();
		UserEntity savedUser = new UserEntity(userToRequest.getUsername(), "password", userToRequest.getEmail());
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());
		given(userRepo.save(any(UserEntity.class))).willReturn(savedUser);
		given(sessionUtils.expireUserSessions(any(UserEntity.class), eq(true), eq(false))).willReturn(savedUser);

		mockMvc.perform(
						post("/app/myProfile/update")
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
		verify(sessionUtils, times(1))
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyProfile_goodUserWithoutLoginChange_success() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateToRequest();
		UserEntity savedUser = new UserEntity(userToRequest.getUsername(), "password", userToRequest.getEmail());
		UserEntity existingUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.of(existingUser));
		given(userRepo.save(any(UserEntity.class))).willReturn(savedUser);

		mockMvc.perform(
						post("/app/myProfile/update")
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
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyProfile_userAlreadyExists_validationError() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareExistingUserDataUpdateToRequest();
		UserEntity existingUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.of(existingUser));

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/myProfile/update")
								.with(user("user").password("password"))
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(view().name("updateMyProfile"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", "username"))
				.andExpect(model().attributeExists("user", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		UserDataUpdate userFromModel = (UserDataUpdate) modelAndView.getModel().get("user");
		Assertions.assertEquals(userToRequest.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(userToRequest.getEmail(), userFromModel.getEmail());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyProfile_incorrectValues_validationError() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareIncorrectUserDataUpdateToRequest();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/myProfile/update")
								.with(user("user").password("password"))
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(view().name("updateMyProfile"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", "username"))
				.andExpect(model().attributeHasFieldErrors("user", "email"))
				.andExpect(model().attributeExists("user", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		UserDataUpdate userFromModel = (UserDataUpdate) modelAndView.getModel().get("user");
		Assertions.assertEquals(userToRequest.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(userToRequest.getEmail(), userFromModel.getEmail());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyProfile_userWithTooShortLoginAfterTrim_validationError() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareUserDataUpdateWithTooShortLoginAfterTrimToRequest();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/myProfile/update")
								.with(user("user").password("password"))
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(view().name("updateMyProfile"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", "username"))
				.andExpect(model().attributeExists("user", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		UserDataUpdate userFromModel = (UserDataUpdate) modelAndView.getModel().get("user");
		Assertions.assertEquals(userToRequest.getUsername().trim(), userFromModel.getUsername());
		Assertions.assertEquals(userToRequest.getEmail(), userFromModel.getEmail());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyProfile_emptyValues_validationError() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareEmptyUserDataUpdateToRequest();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/myProfile/update")
								.with(user("user").password("password"))
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(view().name("updateMyProfile"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", "username"))
				.andExpect(model().attributeHasFieldErrors("user", "email"))
				.andExpect(model().attributeExists("user", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		UserDataUpdate userFromModel = (UserDataUpdate) modelAndView.getModel().get("user");
		Assertions.assertNull(userFromModel.getUsername());
		Assertions.assertNull(userFromModel.getEmail());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(false));
	}

	@Test
	void updateMyPasswordView_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/myProfile/updatePassword"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());
	}

	@Test
	void updateMyPasswordView_loggedCommonUser_success() throws Exception {
		mockMvc.perform(
						get("/app/myProfile/updatePassword")
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
						post("/app/myProfile/updatePassword")
								.with(csrf())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void updateMyPassword_withoutCsrfToken_forbidden() throws Exception {
		UserPasswordUpdate userToRequest = UserDataProvider.prepareGoodUserPasswordUpdateToRequest();

		mockMvc.perform(
						post("/app/myProfile/updatePassword")
								.with(user("user").password("password"))
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(status().isForbidden());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void updateMyPassword_withCsrfInvalidToken_forbidden() throws Exception {
		UserPasswordUpdate userToRequest = UserDataProvider.prepareGoodUserPasswordUpdateToRequest();

		mockMvc.perform(
						post("/app/myProfile/updatePassword")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(status().isForbidden());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void updateMyPassword_simpleCase_success() throws Exception {
		UserPasswordUpdate userToRequest = UserDataProvider.prepareGoodUserPasswordUpdateToRequest();
		given(userRepo.save(any(UserEntity.class))).willReturn(commonUser);
		given(sessionUtils.expireUserSessions(any(UserEntity.class), eq(false), eq(false))).willReturn(commonUser);

		mockMvc.perform(
						post("/app/myProfile/updatePassword")
								.with(user("user").password("password"))
								.with(csrf())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(".."))
				.andExpect(view().name("redirect:.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(sessionUtils, times(1))
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void updateMyPassword_userWithSpacesInPassword_success() throws Exception {
		UserPasswordUpdate userToRequest = UserDataProvider.prepareUserPasswordUpdateWithSpacesInPassToRequest();
		given(userRepo.save(any(UserEntity.class))).willReturn(userWithSpacesInPass);
		given(sessionUtils.expireUserSessions(any(UserEntity.class), eq(false), eq(false))).willReturn(userWithSpacesInPass);

		mockMvc.perform(
						post("/app/myProfile/updatePassword")
								.with(user("user3").password(" pass "))
								.with(csrf())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(".."))
				.andExpect(view().name("redirect:.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("user3").withRoles("USER"));

		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(sessionUtils, times(1))
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void updateMyPassword_incorrectCurrentPassword_validationError() throws Exception {
		UserPasswordUpdate userToRequest = UserDataProvider.prepareUserPasswordUpdateWithIncorrectCurrentPasswordToRequest();

		mockMvc.perform(
						post("/app/myProfile/updatePassword")
								.with(user("user").password("password"))
								.with(csrf())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(view().name("updateMyPassword"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user2", "currentPassword"))
				.andExpect(model().attributeExists("userLogin", "user2"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void updateMyPassword_differentPasswordInConfirmation_validationError() throws Exception {
		UserPasswordUpdate userToRequest = UserDataProvider.prepareUserPasswordUpdateWithConfirmationErrorToRequest();

		mockMvc.perform(
						post("/app/myProfile/updatePassword")
								.with(user("user").password("password"))
								.with(csrf())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(view().name("updateMyPassword"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user2", "confirmPassword"))
				.andExpect(model().attributeExists("userLogin", "user2"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void updateMyPassword_theSamePasswordAsPrevious_validationError() throws Exception {
		UserPasswordUpdate userToRequest = UserDataProvider.prepareUserPasswordUpdateWithTheSamePasswordAsPreviousToRequest();

		mockMvc.perform(
						post("/app/myProfile/updatePassword")
								.with(user("user").password("password"))
								.with(csrf())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(view().name("updateMyPassword"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user2", "password"))
				.andExpect(model().attributeExists("userLogin", "user2"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void updateMyPassword_tooShortPassword_validationError() throws Exception {
		UserPasswordUpdate userToRequest = UserDataProvider.prepareUserPasswordUpdateWithTooShortPasswordToRequest();

		mockMvc.perform(
						post("/app/myProfile/updatePassword")
								.with(user("user").password("password"))
								.with(csrf())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(view().name("updateMyPassword"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user2", "password"))
				.andExpect(model().attributeHasFieldErrors("user2", "confirmPassword"))
				.andExpect(model().attributeExists("userLogin", "user2"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void updateMyPassword_emptyFields_validationError() throws Exception {
		UserPasswordUpdate userToRequest = UserDataProvider.prepareEmptyUserPasswordUpdateToRequest();

		mockMvc.perform(
						post("/app/myProfile/updatePassword")
								.with(user("user").password("password"))
								.with(csrf())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(view().name("updateMyPassword"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user2", "currentPassword"))
				.andExpect(model().attributeHasFieldErrors("user2", "password"))
				.andExpect(model().attributeHasFieldErrors("user2", "confirmPassword"))
				.andExpect(model().attributeExists("userLogin", "user2"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}


	@Test
	void endOtherSessions_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/myProfile/endOtherSessions"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());

		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void endOtherSessions_simpleCase_success() throws Exception {
		given(userRepo.save(any(UserEntity.class))).willReturn(commonUser);
		given(sessionUtils.expireUserSessions(any(UserEntity.class), eq(false), eq(false))).willReturn(commonUser);

		mockMvc.perform(
						get("/app/myProfile/endOtherSessions")
								.with(user("user").password("password")))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:.."))
				.andExpect(redirectedUrl(".."))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(sessionUtils, times(1))
				.expireUserSessions(any(UserEntity.class), eq(false), eq(false));
	}

	@Test
	void deleteUserConfirmation_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/myProfile/delete"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());
	}

	@Test
	void deleteUserConfirmation_loggedCommonUser_success() throws Exception {
		UserEntity expectedUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/myProfile/delete")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("deleteMyProfile"))
				.andExpect(model().attributeExists("userLogin", "user3"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		UserEntity userFromModel = (UserEntity) modelAndView.getModel().get("user3");
		Assertions.assertEquals(expectedUser.getId(), userFromModel.getId());
		Assertions.assertEquals(expectedUser.getCreatedAt(), userFromModel.getCreatedAt());
		Assertions.assertEquals(expectedUser.getUpdatedAt(), userFromModel.getUpdatedAt());
		Assertions.assertEquals(expectedUser.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(expectedUser.getRole(), userFromModel.getRole());
		Assertions.assertEquals(expectedUser.getEmail(), userFromModel.getEmail());
		Assertions.assertEquals(expectedUser.isEnabled(), userFromModel.isEnabled());
	}

	@Test
	void deleteUser_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						post("/app/myProfile/delete")
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());

		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(true));
		verify(userRepo, never())
				.delete(any(UserEntity.class));
	}

	@Test
	void deleteUser_withoutCsrfToken_forbidden() throws Exception {
		mockMvc.perform(
						post("/app/myProfile/delete")
								.with(user("user").password("password")))
				.andExpect(status().isForbidden());

		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(true));
		verify(userRepo, never())
				.delete(any(UserEntity.class));
	}

	@Test
	void deleteUser_withCsrfInvalidToken_forbidden() throws Exception {
		mockMvc.perform(
						post("/app/myProfile/delete")
								.with(csrf().useInvalidToken())
								.with(user("user").password("password")))
				.andExpect(status().isForbidden());

		verify(sessionUtils, never())
				.expireUserSessions(any(UserEntity.class), eq(true), eq(true));
		verify(userRepo, never())
				.delete(any(UserEntity.class));
	}

	@Test
	void deleteUser_simpleCase_success() throws Exception {
		mockMvc.perform(
						post("/app/myProfile/delete")
								.with(user("user").password("password"))
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:../../.."))
				.andExpect(redirectedUrl("../../.."))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(sessionUtils, times(1))
				.expireUserSessions(any(UserEntity.class), eq(true), eq(true));
		verify(userRepo, times(1))
				.delete(any(UserEntity.class));
	}
}
