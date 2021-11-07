package pl.marcinm312.springdatasecurityex.config.security.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import pl.marcinm312.springdatasecurityex.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.config.security.SecurityMessagesConfig;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springdatasecurityex.user.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = LoginApiController.class)
@ComponentScan(basePackageClasses = LoginApiController.class,
			useDefaultFilters = false,
			includeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = LoginApiController.class)
			})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
@SpyBeans({@SpyBean(UserDetailsServiceImpl.class), @SpyBean(RestAuthenticationSuccessHandler.class),
		@SpyBean(RestAuthenticationFailureHandler.class)})
@WebAppConfiguration
class LoginApiControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private UserRepo userRepo;

	@BeforeEach
	void setup() {
		given(userRepo.findByUsername("user")).willReturn(Optional.of(UserDataProvider.prepareExampleGoodUserWithEncodedPassword()));
		given(userRepo.findByUsername("administrator")).willReturn(Optional.of(UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword()));
		given(userRepo.findByUsername("lalala")).willReturn(Optional.empty());
		given(userRepo.findByUsername("user3")).willReturn(Optional.of(UserDataProvider.prepareExampleSecondDisabledUserWithEncodedPassword()));

		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.webApplicationContext)
				.apply(springSecurity())
				.alwaysDo(print())
				.build();
	}

	@ParameterizedTest(name = "{index} ''{2}''")
	@MethodSource("examplesOfAuthorized")
	void login_administratorWithGoodCredentials_success(String username, String password, String nameOfTestCase)
			throws Exception {

		String token = mockMvc.perform(post("/api/login")
						.content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
						.characterEncoding("UTF8"))
				.andExpect(status().isOk())
				.andExpect(header().exists("Authorization"))
				.andReturn().getResponse().getHeader("Authorization");

		assert token != null;
		Assertions.assertTrue(token.startsWith("Bearer "));
	}

	@ParameterizedTest(name = "{index} ''{3}''")
	@MethodSource("examplesOfUnauthenticatedErrors")
	void login_userWithBadCredentials_unauthenticated(String username, String password, String expectedErrorMessage,
													  String nameOfTestCase) throws Exception {

		String receivedErrorMessage = mockMvc.perform(post("/api/login")
						.content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
						.characterEncoding("UTF8"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().doesNotExist("Authorization"))
				.andReturn().getResponse().getErrorMessage();

		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("examplesOfBadRequests")
	void login_incorrectRequestBody_badRequest(String requestBody, String nameOfTestCase) throws Exception {

		mockMvc.perform(post("/api/login")
						.content(requestBody)
						.characterEncoding("UTF8"))
				.andExpect(status().isBadRequest())
				.andExpect(header().doesNotExist("Authorization"));
	}

	private static Stream<Arguments> examplesOfAuthorized() {
		return Stream.of(
				Arguments.of("user", "password", "login_userWithGoodCredentials_success"),
				Arguments.of("administrator", "password", "login_administratorWithGoodCredentials_success")
		);
	}

	private static Stream<Arguments> examplesOfUnauthenticatedErrors() {
		return Stream.of(
				Arguments.of("user", "invalid", "Niepoprawne dane uwierzytelniające",
						"login_userWithBadCredentials_unauthenticated"),
				Arguments.of("administrator", "invalid", "Niepoprawne dane uwierzytelniające",
						"login_administratorWithBadCredentials_unauthenticated"),
				Arguments.of("lalala", "password", "Niepoprawne dane uwierzytelniające",
						"login_notExistingUser_unauthenticated"),
				Arguments.of("user3", "password", "Konto użytkownika jest wyłączone",
						"login_disabledUser_unauthenticated")
		);
	}

	private static Stream<Arguments> examplesOfBadRequests() {
		return Stream.of(
			Arguments.of("aaa", "login_incorrectRequestBody_badRequest1"),
			Arguments.of("{\"username\": \"aaa\", \"password\": \"aaa\"", "login_incorrectRequestBody_badRequest2"),
			Arguments.of("{\"username\": \"aaa\", \"password\": \"aaa}", "login_incorrectRequestBody_badRequest3"),
			Arguments.of("{\"username\": \"aaa\", \"password\": \"aaa", "login_incorrectRequestBody_badRequest4"),
			Arguments.of("", "login_incorrectRequestBody_badRequest5"),
			Arguments.of("{..}", "login_incorrectRequestBody_badRequest6")
		);
	}
}