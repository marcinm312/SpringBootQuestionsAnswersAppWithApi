package pl.marcinm312.springdatasecurityex.controller.web;

import org.junit.jupiter.api.Assertions;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import pl.marcinm312.springdatasecurityex.config.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.model.answer.Answer;
import pl.marcinm312.springdatasecurityex.model.answer.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.model.question.Question;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.repository.AnswerRepository;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.MailService;
import pl.marcinm312.springdatasecurityex.service.db.AnswerManager;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;
import pl.marcinm312.springdatasecurityex.testdataprovider.AnswerDataProvider;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.utils.SessionUtils;

import javax.mail.MessagingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AnswerWebController.class)
@ComponentScan(basePackageClasses = AnswerWebController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = AnswerWebController.class)
		})
@MockBeans({@MockBean(TokenRepo.class), @MockBean(SessionUtils.class)})
@SpyBeans({@SpyBean(QuestionManager.class), @SpyBean(AnswerManager.class), @SpyBean(ExcelGenerator.class),
		@SpyBean(PdfGenerator.class), @SpyBean(UserDetailsServiceImpl.class), @SpyBean(UserManager.class)})
@Import({MultiHttpSecurityCustomConfig.class})
class AnswerWebControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private QuestionRepository questionRepository;

	@MockBean
	private AnswerRepository answerRepository;

	@MockBean
	private UserRepo userRepo;

	@MockBean
	private MailService mailService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final User commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final User secondUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
	private final User adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();

	@BeforeEach
	void setup() throws MessagingException {
		Answer answer = AnswerDataProvider.prepareExampleAnswer();
		Question question = QuestionDataProvider.prepareExampleQuestion();
		doNothing().when(mailService).sendMail(isA(String.class), isA(String.class), isA(String.class), isA(boolean.class));

		given(questionRepository.existsById(1000L)).willReturn(true);
		given(questionRepository.existsById(2000L)).willReturn(false);

		given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
		given(questionRepository.findById(2000L)).willReturn(Optional.empty());

		given(answerRepository.findByQuestionIdOrderByIdDesc(1000L))
				.willReturn(AnswerDataProvider.prepareExampleAnswersList());
		given(answerRepository.findById(1000L)).willReturn(Optional.of(answer));
		given(answerRepository.findById(2000L)).willReturn(Optional.empty());
		doNothing().when(answerRepository).delete(isA(Answer.class));

		given(userRepo.findByUsername("user")).willReturn(Optional.of(commonUser));
		given(userRepo.findByUsername("user2")).willReturn(Optional.of(secondUser));
		given(userRepo.findByUsername("administrator")).willReturn(Optional.of(adminUser));

		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(this.webApplicationContext)
						.apply(springSecurity())
						.alwaysDo(print())
						.build();
	}

	@Test
	@WithAnonymousUser
	void questionsGet_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/answers"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void answersGet_simpleCase_success() throws Exception {
		Question expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		ModelAndView modelAndView = mockMvc.perform(
				get("/app/questions/1000/answers")
						.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("answers"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		List<AnswerGet> answersFromModel = (List<AnswerGet>) modelAndView.getModel().get("answerList");
		int arrayExpectedSize = 3;
		int arrayResultSize = answersFromModel.size();
		Assertions.assertEquals(arrayExpectedSize, arrayResultSize);

		String usernameFromModel = (String) modelAndView.getModel().get("userLogin");
		String expectedUser = "user";
		Assertions.assertEquals(expectedUser, usernameFromModel);

		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());
	}

	@Test
	@WithMockUser(username = "user")
	void answersGet_questionNotExists_notFoundMessage() throws Exception {
		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/2000/answers")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		String usernameFromModel = (String) modelAndView.getModel().get("userLogin");
		String expectedUser = "user";
		Assertions.assertEquals(expectedUser, usernameFromModel);

		String messageFromModel = (String) modelAndView.getModel().get("message");
		String expectedMessage = "Question not found with id 2000";
		Assertions.assertEquals(expectedMessage, messageFromModel);
	}

	@Test
	@WithAnonymousUser
	void downloadPdf_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/answers/pdf-export"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void downloadPdf_simpleCase_success() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/answers/pdf-export")
								.with(httpBasic("user", "password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithAnonymousUser
	void downloadExcel_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/answers/excel-export"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void downloadExcel_simpleCase_success() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/answers/excel-export")
								.with(httpBasic("user", "password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@WithMockUser(username = "user")
	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("examplesOfQuestionNotFoundUrls")
	void downloadFile_questionNotExists_notFound(String url, String nameOfTestCase) throws Exception {
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						get(url).with(httpBasic("user", "password")))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(new MediaType("text" , "plain", StandardCharsets.UTF_8)))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResponse().getContentAsString());

		String expectedErrorMessage = "Question not found with id 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	private static Stream<Arguments> examplesOfQuestionNotFoundUrls() {
		return Stream.of(
				Arguments.of("/app/questions/2000/answers/pdf-export",
						"downloadPdf_questionNotExists_notFound"),
				Arguments.of("/app/questions/2000/answers/excel-export",
						"downloadExcel_questionNotExists_notFound")
		);
	}
}