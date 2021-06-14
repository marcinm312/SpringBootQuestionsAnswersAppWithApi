package pl.marcinm312.springdatasecurityex.controller.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.mock.mockito.SpyBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springdatasecurityex.config.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.db.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
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
@Import({MultiHttpSecurityCustomConfig.class})
@SpyBeans({@SpyBean(UserDetailsServiceImpl.class)})
@WebAppConfiguration
class MainWebControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private UserRepo userRepo;

	@BeforeEach
	void setUp() {
		given(userRepo.findByUsername("user")).willReturn(Optional.of(UserDataProvider.prepareExampleGoodUserWithEncodedPassword()));
		given(userRepo.findByUsername("administrator")).willReturn(Optional.of(UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword()));
		given(userRepo.findByUsername("lalala")).willReturn(Optional.empty());

		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.webApplicationContext)
				.apply(springSecurity())
				.alwaysDo(print())
				.build();
	}

	@Test
	@WithAnonymousUser
	void getMainPage_simpleCase_success() throws Exception {
		mockMvc.perform(
				get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("main"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithAnonymousUser
	void getCss_simpleCase_success() throws Exception {
		mockMvc.perform(
				get("/css/style.css"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/css"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithAnonymousUser
	void formLogin_userWithGoodCredentials_success() throws Exception {
		mockMvc.perform(
				formLogin().user("user").password("password"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithAnonymousUser
	void formLogin_administratorWithGoodCredentials_success() throws Exception {
		mockMvc.perform(
				formLogin().user("administrator").password("password"))
				.andExpect(authenticated().withUsername("administrator").withRoles("ADMIN"));
	}

	@Test
	@WithAnonymousUser
	void formLogin_userWithBadCredentials_unauthenticated() throws Exception {
		mockMvc.perform(
				formLogin().user("user").password("invalid"))
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithAnonymousUser
	void formLogin_administratorWithBadCredentials_unauthenticated() throws Exception {
		mockMvc.perform(
				formLogin().user("administrator").password("invalid"))
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithAnonymousUser
	void formLogin_notExistingUser_unauthenticated() throws Exception {
		mockMvc.perform(
				formLogin().user("lalala").password("password"))
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithAnonymousUser
	void logout_simpleCase_success() throws Exception {
		mockMvc.perform(
				logout())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(unauthenticated());
	}
}