package pl.marcinm312.springquestionsanswers.main.controller;

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
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = MainWebController.class)
@ComponentScan(basePackageClasses = MainWebController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = MainWebController.class)
		})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
@SpyBeans({@SpyBean(UserDetailsServiceImpl.class), @SpyBean(RestAuthenticationSuccessHandler.class),
		@SpyBean(RestAuthenticationFailureHandler.class)})
@WebAppConfiguration
class MainWebControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private UserRepo userRepo;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();

	@BeforeEach
	void setUp() {

		given(userRepo.findByUsername("user")).willReturn(Optional.of(commonUser));

		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.webApplicationContext)
				.apply(springSecurity())
				.alwaysDo(print())
				.build();
	}

	@Test
	void getMainPage_simpleCase_success() throws Exception {

		mockMvc.perform(
				get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("main"))
				.andExpect(unauthenticated());
	}

	@ParameterizedTest
	@MethodSource("examplesOfStaticResources")
	void getStaticResource_simpleCase_success(String url, String contentType) throws Exception {

		mockMvc.perform(
						get(url))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType))
				.andExpect(unauthenticated());
	}

	private static Stream<Arguments> examplesOfStaticResources() {

		return Stream.of(
			Arguments.of("/css/style.css", "text/css"),
			Arguments.of("/css/signin.css", "text/css"),
			Arguments.of("/js/clearPasswordsFieldsInRegistrationForm.js", "text/javascript")
		);
	}

	@Test
	void getSecuredStaticResource_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/js/clearChangePasswordForm.js"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@Test
	void getSecuredStaticResource_simpleCase_success() throws Exception {

		mockMvc.perform(
						get("/js/clearChangePasswordForm.js")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/javascript"));
	}
}
