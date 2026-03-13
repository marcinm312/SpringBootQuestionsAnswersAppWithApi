package pl.marcinm312.springquestionsanswers.main.controller;

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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pl.marcinm312.springquestionsanswers.answer.repository.AnswerRepository;
import pl.marcinm312.springquestionsanswers.mail.repository.MailRepository;
import pl.marcinm312.springquestionsanswers.question.repository.QuestionRepository;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.repository.ActivationTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.MailChangeTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
class MainWebControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserRepo userRepo;

	@MockitoBean
	private AnswerRepository answerRepository;

	@MockitoBean
	private QuestionRepository questionRepository;

	@MockitoBean
	private JavaMailSender javaMailSender;

	@MockitoBean
	private MailRepository mailRepository;

	@MockitoBean
	private ActivationTokenRepo activationTokenRepo;

	@MockitoBean
	private MailChangeTokenRepo mailChangeTokenRepo;


	private final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();

	@BeforeEach
	void setUp() {

		given(userRepo.findByUsername("user")).willReturn(Optional.of(commonUser));
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
