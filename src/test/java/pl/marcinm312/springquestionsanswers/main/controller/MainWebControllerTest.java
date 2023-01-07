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
import org.springframework.boot.test.mock.mockito.MockBeans;
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
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;

import java.util.stream.Stream;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
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
@MockBeans({@MockBean(UserRepo.class)})
@SpyBeans({@SpyBean(UserDetailsServiceImpl.class), @SpyBean(RestAuthenticationSuccessHandler.class),
		@SpyBean(RestAuthenticationFailureHandler.class)})
@WebAppConfiguration
class MainWebControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	void setUp() {
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

	@ParameterizedTest(name = "{index} ''{2}''")
	@MethodSource("examplesOfStaticResources")
	void getStaticResource_simpleCase_success(String url, String contentType, String nameOfTestCase) throws Exception {
		mockMvc.perform(
						get(url))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType))
				.andExpect(unauthenticated());
	}

	private static Stream<Arguments> examplesOfStaticResources() {
		return Stream.of(
			Arguments.of("/css/style.css", "text/css", "getCss_simpleCase_success"),
			Arguments.of("/css/signin.css", "text/css", "getSignInCss_simpleCase_success"),
			Arguments.of("/js/clearPasswordsFieldsInRegistrationForm.js", "application/javascript",
					"getJsScriptInRegistrationForm_simpleCase_success")
		);
	}
}
