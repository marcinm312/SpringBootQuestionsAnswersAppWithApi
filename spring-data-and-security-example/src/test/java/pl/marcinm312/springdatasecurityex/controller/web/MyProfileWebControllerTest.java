package pl.marcinm312.springdatasecurityex.controller.web;

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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import pl.marcinm312.springdatasecurityex.config.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserDataUpdate;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserPasswordUpdate;
import pl.marcinm312.springdatasecurityex.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.MailService;
import pl.marcinm312.springdatasecurityex.service.db.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.utils.SessionUtils;
import pl.marcinm312.springdatasecurityex.validator.UserDataUpdateValidator;
import pl.marcinm312.springdatasecurityex.validator.UserPasswordUpdateValidator;

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
		@SpyBean(UserDataUpdateValidator.class), @SpyBean(UserPasswordUpdateValidator.class)})
@Import({MultiHttpSecurityCustomConfig.class})
class MyProfileWebControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private UserRepo userRepo;

	@MockBean
	private SessionUtils sessionUtils;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	void setup() {
		User commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(userRepo.findByUsername("user")).willReturn(Optional.of(commonUser));

		User adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();
		given(userRepo.findByUsername("administrator")).willReturn(Optional.of(adminUser));

		doNothing().when(userRepo).delete(isA(User.class));

		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(this.webApplicationContext)
						.apply(springSecurity())
						.alwaysDo(print())
						.build();
	}

	@Test
	@WithAnonymousUser
	void myProfileView_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/myProfile"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void myProfileView_loggedCommonUser_success() throws Exception {
		User expectedUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/myProfile")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("myProfile"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		User userFromModel = (User) modelAndView.getModel().get("user3");
		Assertions.assertEquals(expectedUser.getId(), userFromModel.getId());
		Assertions.assertEquals(expectedUser.getCreatedAt(), userFromModel.getCreatedAt());
		Assertions.assertEquals(expectedUser.getUpdatedAt(), userFromModel.getUpdatedAt());
		Assertions.assertEquals(expectedUser.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(expectedUser.getRole(), userFromModel.getRole());
		Assertions.assertEquals(expectedUser.getEmail(), userFromModel.getEmail());
		Assertions.assertEquals(expectedUser.isEnabled(), userFromModel.isEnabled());
	}

	@Test
	@WithAnonymousUser
	void updateMyProfileView_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/myProfile/update"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void updateMyProfileView_loggedCommonUser_success() throws Exception {
		User expectedUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();

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
	@WithAnonymousUser
	void updateMyProfile_withAnonymousUser_redirectToLoginPage() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest();

		mockMvc.perform(
						post("/app/myProfile/update")
								.with(csrf())
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(String.class), eq(true));
	}

	@Test
	@WithMockUser(username = "user")
	void updateMyProfile_withoutCsrfToken_forbidden() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest();

		mockMvc.perform(
						post("/app/myProfile/update")
								.with(user("user").password("password"))
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isForbidden());

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions("user", true);
		verify(sessionUtils, never())
				.expireUserSessions("user3", true);
	}

	@Test
	@WithMockUser(username = "user")
	void updateMyProfile_withCsrfInvalidToken_forbidden() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest();

		mockMvc.perform(
						post("/app/myProfile/update")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken())
								.param("username", userToRequest.getUsername())
								.param("email", userToRequest.getEmail()))
				.andExpect(status().isForbidden());

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions("user", true);
		verify(sessionUtils, never())
				.expireUserSessions("user3", true);
	}

	@Test
	@WithMockUser(username = "user")
	void updateMyProfile_goodUserWithLoginChange_success() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateWithLoginChangeToRequest();
		User user = new User(userToRequest.getUsername(), "password", userToRequest.getEmail());
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());
		given(userRepo.save(any(User.class))).willReturn(user);

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

		verify(userRepo, times(1)).save(any(User.class));
		verify(sessionUtils, times(1))
				.expireUserSessions("user", true);
		verify(sessionUtils, times(1))
				.expireUserSessions("user3", true);
	}

	@Test
	@WithMockUser(username = "user")
	void updateMyProfile_goodUserWithoutLoginChange_success() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareGoodUserDataUpdateToRequest();
		User user = new User(userToRequest.getUsername(), "password", userToRequest.getEmail());
		User existingUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.of(existingUser));
		given(userRepo.save(any(User.class))).willReturn(user);

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

		verify(userRepo, times(1)).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(String.class), eq(true));
	}

	@Test
	@WithMockUser(username = "user")
	void updateMyProfile_userAlreadyExists_validationError() throws Exception {
		UserDataUpdate userToRequest = UserDataProvider.prepareExistingUserDataUpdateToRequest();
		User existingUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		given(userRepo.findByUsername("user2")).willReturn(Optional.of(existingUser));

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

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(String.class), eq(true));
	}

	@Test
	@WithMockUser(username = "user")
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

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(String.class), eq(true));
	}

	@Test
	@WithMockUser(username = "user")
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
		Assertions.assertEquals(userToRequest.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(userToRequest.getEmail(), userFromModel.getEmail());

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(String.class), eq(true));
	}

	@Test
	@WithAnonymousUser
	void updateMyPasswordView_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/myProfile/updatePassword"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
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
	@WithAnonymousUser
	void updateMyPassword_withAnonymousUser_redirectToLoginPage() throws Exception {
		UserPasswordUpdate userToRequest = UserDataProvider.prepareGoodUserPasswordUpdateToRequest();

		mockMvc.perform(
						post("/app/myProfile/updatePassword")
								.with(csrf())
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions(any(String.class), eq(false));
	}

	@Test
	@WithMockUser(username = "user")
	void updateMyPassword_withoutCsrfToken_forbidden() throws Exception {
		UserPasswordUpdate userToRequest = UserDataProvider.prepareGoodUserPasswordUpdateToRequest();

		mockMvc.perform(
						post("/app/myProfile/updatePassword")
								.with(user("user").password("password"))
								.param("currentPassword", userToRequest.getCurrentPassword())
								.param("password", userToRequest.getPassword())
								.param("confirmPassword", userToRequest.getConfirmPassword()))
				.andExpect(status().isForbidden());

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions("user", false);
	}

	@Test
	@WithMockUser(username = "user")
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

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions("user", false);
	}

	@Test
	@WithMockUser(username = "user")
	void updateMyPassword_simpleCase_success() throws Exception {
		UserPasswordUpdate userToRequest = UserDataProvider.prepareGoodUserPasswordUpdateToRequest();
		User user = new User("user", userToRequest.getPassword(), "test@abc.pl");
		given(userRepo.save(any(User.class))).willReturn(user);

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

		verify(userRepo, times(1)).save(any(User.class));
		verify(sessionUtils, times(1))
				.expireUserSessions("user", false);
	}

	@Test
	@WithMockUser(username = "user")
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

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions("user", false);
	}

	@Test
	@WithMockUser(username = "user")
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

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions("user", false);
	}

	@Test
	@WithMockUser(username = "user")
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

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions("user", false);
	}

	@Test
	@WithMockUser(username = "user")
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

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions("user", false);
	}

	@Test
	@WithMockUser(username = "user")
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

		verify(userRepo, never()).save(any(User.class));
		verify(sessionUtils, never())
				.expireUserSessions("user", false);
	}


	@Test
	@WithAnonymousUser
	void endOtherSessions_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/myProfile/endOtherSessions"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());

		verify(sessionUtils, never())
				.expireUserSessions(any(String.class), eq(false));
	}

	@Test
	@WithMockUser(username = "user")
	void endOtherSessions_simpleCase_success() throws Exception {
		mockMvc.perform(
						get("/app/myProfile/endOtherSessions")
								.with(user("user").password("password")))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:.."))
				.andExpect(redirectedUrl(".."))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(sessionUtils, times(1))
				.expireUserSessions("user", false);
	}

	@Test
	@WithAnonymousUser
	void deleteUserConfirmation_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/myProfile/delete"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void deleteUserConfirmation_loggedCommonUser_success() throws Exception {
		User expectedUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();

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

		User userFromModel = (User) modelAndView.getModel().get("user3");
		Assertions.assertEquals(expectedUser.getId(), userFromModel.getId());
		Assertions.assertEquals(expectedUser.getCreatedAt(), userFromModel.getCreatedAt());
		Assertions.assertEquals(expectedUser.getUpdatedAt(), userFromModel.getUpdatedAt());
		Assertions.assertEquals(expectedUser.getUsername(), userFromModel.getUsername());
		Assertions.assertEquals(expectedUser.getRole(), userFromModel.getRole());
		Assertions.assertEquals(expectedUser.getEmail(), userFromModel.getEmail());
		Assertions.assertEquals(expectedUser.isEnabled(), userFromModel.isEnabled());
	}

	@Test
	@WithAnonymousUser
	void deleteUser_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						post("/app/myProfile/delete")
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());

		verify(sessionUtils, never())
				.expireUserSessions(any(String.class), eq(true));
		verify(userRepo, never())
				.delete(any(User.class));
	}

	@Test
	@WithMockUser(username = "user")
	void deleteUser_withoutCsrfToken_forbidden() throws Exception {
		mockMvc.perform(
						post("/app/myProfile/delete")
								.with(user("user").password("password")))
				.andExpect(status().isForbidden());

		verify(sessionUtils, never())
				.expireUserSessions(any(String.class), eq(true));
		verify(userRepo, never())
				.delete(any(User.class));
	}

	@Test
	@WithMockUser(username = "user")
	void deleteUser_withCsrfInvalidToken_forbidden() throws Exception {
		mockMvc.perform(
						post("/app/myProfile/delete")
								.with(csrf().useInvalidToken())
								.with(user("user").password("password")))
				.andExpect(status().isForbidden());

		verify(sessionUtils, never())
				.expireUserSessions(any(String.class), eq(true));
		verify(userRepo, never())
				.delete(any(User.class));
	}

	@Test
	@WithMockUser(username = "user")
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
				.expireUserSessions("user", true);
		verify(userRepo, times(1))
				.delete(any(User.class));
	}
}
