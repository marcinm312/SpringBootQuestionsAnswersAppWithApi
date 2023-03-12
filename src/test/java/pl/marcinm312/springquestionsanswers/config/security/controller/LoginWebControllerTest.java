package pl.marcinm312.springquestionsanswers.config.security.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.mock.mockito.SpyBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springquestionsanswers.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springquestionsanswers.config.security.SecurityMessagesConfig;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = LoginWebController.class)
@ComponentScan(basePackageClasses = LoginWebController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = LoginWebController.class)
		})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
@SpyBeans({@SpyBean(UserDetailsServiceImpl.class), @SpyBean(RestAuthenticationSuccessHandler.class),
		@SpyBean(RestAuthenticationFailureHandler.class)})
@WebAppConfiguration
class LoginWebControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private UserRepo userRepo;

	@BeforeEach
	void setup() {
		given(userRepo.findByUsername("user")).willReturn(Optional.of(UserDataProvider.prepareExampleGoodUserWithEncodedPassword()));
		given(userRepo.findByUsername("admin")).willReturn(Optional.of(UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword()));
		given(userRepo.findByUsername("lalala")).willReturn(Optional.empty());
		given(userRepo.findByUsername("user3")).willReturn(Optional.of(UserDataProvider.prepareExampleSecondDisabledUserWithEncodedPassword()));

		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.webApplicationContext)
				.apply(springSecurity())
				.alwaysDo(print())
				.build();
	}

	@Test
	void getLoginPage_simpleCase_success() throws Exception {
		mockMvc.perform(
						get("/loginPage"))
				.andExpect(status().isOk())
				.andExpect(view().name("loginForm"))
				.andExpect(unauthenticated());
	}

	@Test
	void formLogin_userWithGoodCredentials_success() throws Exception {
		mockMvc.perform(
						formLogin("/authenticate").user("user").password("password"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	void formLogin_administratorWithGoodCredentials_success() throws Exception {
		mockMvc.perform(
						formLogin("/authenticate").user("admin").password("password"))
				.andExpect(authenticated().withUsername("admin").withRoles("ADMIN"));
	}

	@ParameterizedTest(name = "{index} ''{2}''")
	@MethodSource("examplesOfUnauthenticatedErrors")
	void formLogin_badCredentials_unauthenticated(String username, String password, String nameOfTestCase)
			throws Exception {
		mockMvc.perform(
						formLogin("/authenticate").user(username).password(password))
				.andExpect(redirectedUrl("/loginPage?error"))
				.andExpect(unauthenticated());
	}

	private static Stream<Arguments> examplesOfUnauthenticatedErrors() {
		return Stream.of(
				Arguments.of("user", "invalid", "formLogin_userWithBadCredentials_unauthenticated"),
				Arguments.of("admin", "invalid", "formLogin_administratorWithBadCredentials_unauthenticated"),
				Arguments.of("lalala", "password", "formLogin_notExistingUser_unauthenticated"),
				Arguments.of("user3", "password", "formLogin_disabledUser_unauthenticated")
		);
	}

	@Test
	void logout_simpleCase_success() throws Exception {
		mockMvc.perform(
						logout())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(unauthenticated());
	}
}
