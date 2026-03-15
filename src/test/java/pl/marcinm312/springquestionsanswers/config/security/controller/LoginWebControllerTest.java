package pl.marcinm312.springquestionsanswers.config.security.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pl.marcinm312.springquestionsanswers.answer.repository.AnswerRepository;
import pl.marcinm312.springquestionsanswers.mail.service.MailService;
import pl.marcinm312.springquestionsanswers.question.repository.QuestionRepository;
import pl.marcinm312.springquestionsanswers.user.repository.ActivationTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.MailChangeTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserAdminManager;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.SYSTEM_OUT, printOnlyOnFailure = false)
@EnableAutoConfiguration(exclude = {
		org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration.class,
		org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
		org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
		org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
})
@MockitoBean(types = {AnswerRepository.class, QuestionRepository.class, ActivationTokenRepo.class,
		MailChangeTokenRepo.class, MailService.class, UserAdminManager.class})
class LoginWebControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserRepo userRepo;


	@BeforeEach
	void setup() {

		given(userRepo.findByUsername("user")).willReturn(Optional.of(UserDataProvider
				.prepareExampleGoodUserWithEncodedPassword()));
		given(userRepo.findByUsername("admin")).willReturn(Optional.of(UserDataProvider
				.prepareExampleGoodAdministratorWithEncodedPassword()));
		given(userRepo.findByUsername("lalala")).willReturn(Optional.empty());
		given(userRepo.findByUsername("user3")).willReturn(Optional.of(UserDataProvider
				.prepareExampleSecondDisabledUserWithEncodedPassword()));
		given(userRepo.findByUsername("user5")).willReturn(Optional.of(UserDataProvider.
				prepareExampleLockedUserWithEncodedPassword()));
		given(userRepo.findByUsername("user6")).willReturn(Optional.of(UserDataProvider.
				prepareExampleDisabledAndLockedUserWithEncodedPassword()));
	}

	@Test
	void getLoginPage_simpleCase_success() throws Exception {

		mockMvc.perform(
						get("/loginPage/"))
				.andExpect(status().isOk())
				.andExpect(view().name("loginForm"))
				.andExpect(unauthenticated());
	}

	@Test
	void formLogin_userWithGoodCredentials_success() throws Exception {

		mockMvc.perform(
						formLogin("/authenticate/").user("user").password("password"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	void formLogin_administratorWithGoodCredentials_success() throws Exception {

		mockMvc.perform(
						formLogin("/authenticate/").user("admin").password("password"))
				.andExpect(authenticated().withUsername("admin").withRoles("ADMIN"));
	}

	@ParameterizedTest
	@MethodSource("examplesOfUnauthenticatedErrors")
	void formLogin_badCredentials_unauthenticated(String username, String password) throws Exception {

		mockMvc.perform(
						formLogin("/authenticate/").user(username).password(password))
				.andExpect(redirectedUrl("/loginPage/?error"))
				.andExpect(unauthenticated());
	}

	private static Stream<Arguments> examplesOfUnauthenticatedErrors() {

		return Stream.of(
				Arguments.of("user", "invalid"),
				Arguments.of("admin", "invalid"),
				Arguments.of("lalala", "password"),
				Arguments.of("user3", "password"),
				Arguments.of("user5", "password"),
				Arguments.of("user6", "password")
		);
	}

	@Test
	void logout_simpleCase_success() throws Exception {

		mockMvc.perform(
						get("/logout"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(unauthenticated());
	}
}
