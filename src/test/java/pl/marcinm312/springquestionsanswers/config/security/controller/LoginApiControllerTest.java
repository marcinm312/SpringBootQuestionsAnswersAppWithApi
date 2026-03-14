package pl.marcinm312.springquestionsanswers.config.security.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.SYSTEM_OUT, printOnlyOnFailure = false)
@EnableAutoConfiguration(exclude = {
		org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration.class,
		org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
		org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
		org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
})
class LoginApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserRepo userRepo;

	@MockitoBean
	private AnswerRepository answerRepository;

	@MockitoBean
	private QuestionRepository questionRepository;

	@MockitoBean
	private ActivationTokenRepo activationTokenRepo;

	@MockitoBean
	private MailChangeTokenRepo mailChangeTokenRepo;

	@MockitoBean
	private MailService mailService;

	@MockitoBean
	private UserAdminManager userAdminManager;


	@BeforeEach
	void setup() {

		given(userRepo.findByUsername("user")).willReturn(Optional.of(UserDataProvider.
				prepareExampleGoodUserWithEncodedPassword()));
		given(userRepo.findByUsername("admin")).willReturn(Optional.of(UserDataProvider.
				prepareExampleGoodAdministratorWithEncodedPassword()));
		given(userRepo.findByUsername("lalala")).willReturn(Optional.empty());
		given(userRepo.findByUsername("user3")).willReturn(Optional.of(UserDataProvider.
				prepareExampleSecondDisabledUserWithEncodedPassword()));
		given(userRepo.findByUsername("user5")).willReturn(Optional.of(UserDataProvider.
				prepareExampleLockedUserWithEncodedPassword()));
		given(userRepo.findByUsername("user6")).willReturn(Optional.of(UserDataProvider.
				prepareExampleDisabledAndLockedUserWithEncodedPassword()));
	}

	@ParameterizedTest
	@MethodSource("examplesOfAuthorized")
	void login_administratorWithGoodCredentials_success(String username, String password) throws Exception {

		String token = mockMvc.perform(post("/api/login")
						.content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
						.characterEncoding("UTF8"))
				.andExpect(status().isOk())
				.andExpect(header().exists("Authorization"))
				.andReturn().getResponse().getHeader("Authorization");

		assert token != null;
		Assertions.assertTrue(token.startsWith("Bearer "));
	}

	@ParameterizedTest
	@MethodSource("examplesOfUnauthenticatedErrors")
	void login_userWithBadCredentials_unauthenticated(String username, String password, String expectedErrorMessage)
			throws Exception {

		String receivedErrorMessage = mockMvc.perform(post("/api/login")
						.content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
						.characterEncoding("UTF8")
						.locale(Locale.of("pl", "PL")))
				.andExpect(status().isUnauthorized())
				.andExpect(header().doesNotExist("Authorization"))
				.andReturn().getResponse().getErrorMessage();

		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	@ParameterizedTest
	@MethodSource("examplesOfBadRequests")
	void login_incorrectRequestBody_badRequest(String requestBody) throws Exception {

		mockMvc.perform(post("/api/login")
						.content(requestBody)
						.characterEncoding("UTF8"))
				.andExpect(status().isBadRequest())
				.andExpect(header().doesNotExist("Authorization"));
	}

	private static Stream<Arguments> examplesOfAuthorized() {

		return Stream.of(
				Arguments.of("user", "password"),
				Arguments.of("admin", "password")
		);
	}

	private static Stream<Arguments> examplesOfUnauthenticatedErrors() {

		return Stream.of(
				Arguments.of("user", "invalid", "Niepoprawne dane uwierzytelniające"),
				Arguments.of("admin", "invalid", "Niepoprawne dane uwierzytelniające"),
				Arguments.of("lalala", "password", "Niepoprawne dane uwierzytelniające"),
				Arguments.of("user3", "password", "Konto użytkownika jest wyłączone"),
				Arguments.of("user5", "password", "Konto użytkownika jest zablokowane"),
				Arguments.of("user6", "password", "Konto użytkownika jest zablokowane")
		);
	}

	private static Stream<Arguments> examplesOfBadRequests() {

		return Stream.of(
				Arguments.of("aaa"),
				Arguments.of("{\"username\": \"aaa\", \"password\": \"aaa\""),
				Arguments.of("{\"username\": \"aaa\", \"password\": \"aaa}"),
				Arguments.of("{\"username\": \"aaa\", \"password\": \"aaa"),
				Arguments.of(""),
				Arguments.of("{..}")
		);
	}
}
