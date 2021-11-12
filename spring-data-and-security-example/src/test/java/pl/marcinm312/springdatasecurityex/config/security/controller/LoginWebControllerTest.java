package pl.marcinm312.springdatasecurityex.config.security.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

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
		given(userRepo.findByUsername("administrator")).willReturn(Optional.of(UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword()));
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
						formLogin("/authenticate").user("administrator").password("password"))
				.andExpect(authenticated().withUsername("administrator").withRoles("ADMIN"));
	}

	@Test
	void formLogin_userWithBadCredentials_unauthenticated() throws Exception {
		mockMvc.perform(
						formLogin("/authenticate").user("user").password("invalid"))
				.andExpect(redirectedUrl("/loginPage?error"))
				.andExpect(unauthenticated());
	}

	@Test
	void formLogin_administratorWithBadCredentials_unauthenticated() throws Exception {
		mockMvc.perform(
						formLogin("/authenticate").user("administrator").password("invalid"))
				.andExpect(redirectedUrl("/loginPage?error"))
				.andExpect(unauthenticated());
	}

	@Test
	void formLogin_notExistingUser_unauthenticated() throws Exception {
		mockMvc.perform(
						formLogin("/authenticate").user("lalala").password("password"))
				.andExpect(redirectedUrl("/loginPage?error"))
				.andExpect(unauthenticated());
	}

	@Test
	void formLogin_disabledUser_unauthenticated() throws Exception {
		mockMvc.perform(
						formLogin("/authenticate").user("user3").password("password"))
				.andExpect(redirectedUrl("/loginPage?error"))
				.andExpect(unauthenticated());
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
