package pl.marcinm312.springdatasecurityex.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springdatasecurityex.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.config.security.SecurityMessagesConfig;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springdatasecurityex.model.answer.Answer;
import pl.marcinm312.springdatasecurityex.model.answer.dto.AnswerCreateUpdate;
import pl.marcinm312.springdatasecurityex.model.answer.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.model.question.Question;
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
import pl.marcinm312.springdatasecurityex.testdataprovider.AnswerDataProvider;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.utils.SessionUtils;
import pl.marcinm312.springdatasecurityex.utils.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.utils.file.PdfGenerator;

import javax.mail.MessagingException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AnswerApiController.class)
@ComponentScan(basePackageClasses = AnswerApiController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = AnswerApiController.class)
		})
@MockBeans({@MockBean(TokenRepo.class), @MockBean(SessionUtils.class)})
@SpyBeans({@SpyBean(QuestionManager.class), @SpyBean(AnswerManager.class), @SpyBean(UserDetailsServiceImpl.class),
		@SpyBean(UserManager.class), @SpyBean(ExcelGenerator.class), @SpyBean(PdfGenerator.class),
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
class AnswerApiControllerTest {

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

	private final ObjectMapper mapper = new ObjectMapper();

	private final User commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final User secondUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
	private final User adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();

	private final Question question = QuestionDataProvider.prepareExampleQuestion();

	@BeforeEach
	void setup() throws MessagingException {
		Answer answer = AnswerDataProvider.prepareExampleAnswer();
		doNothing().when(mailService).sendMail(isA(String.class), isA(String.class), isA(String.class), isA(boolean.class));

		given(questionRepository.existsById(1000L)).willReturn(true);
		given(questionRepository.existsById(2000L)).willReturn(false);

		given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
		given(questionRepository.findById(2000L)).willReturn(Optional.empty());

		given(answerRepository.findByQuestionIdOrderByIdDesc(1000L))
				.willReturn(AnswerDataProvider.prepareExampleAnswersList());
		given(answerRepository.findByQuestionIdAndId(1000L, 1000L)).willReturn(Optional.of(answer));
		given(answerRepository.findByQuestionIdAndId(1000L, 2000L)).willReturn(Optional.empty());
		given(answerRepository.findByQuestionIdAndId(2000L, 1000L)).willReturn(Optional.empty());
		given(answerRepository.findByQuestionIdAndId(2000L, 2000L)).willReturn(Optional.empty());
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
	void getAnswersByQuestionId_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						get("/api/questions/1000/answers"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getAnswersByQuestionId_simpleCase_success() throws Exception {

		String token = prepareToken("user", "password");

		String response = mockMvc.perform(
						get("/api/questions/1000/answers")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		AnswerGet[] responseAnswerList = mapper.readValue(response, AnswerGet[].class);
		int arrayExpectedSize = 3;
		int arrayResultSize = responseAnswerList.length;
		Assertions.assertEquals(arrayExpectedSize, arrayResultSize);
	}

	@Test
	void getAnswersByQuestionId_questionNotExists_notFound() throws Exception {

		String token = prepareToken("user", "password");

		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						get("/api/questions/2000/answers")
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Question not found with id: 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	@Test
	void getAnswerByQuestionIdAndAnswerId_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						get("/api/questions/1000/answers/1000"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getAnswerByQuestionIdAndAnswerId_simpleCase_success() throws Exception {

		String token = prepareToken("user", "password");

		Answer answer = AnswerDataProvider.prepareExampleAnswer();
		String response = mockMvc.perform(
						get("/api/questions/1000/answers/1000")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		AnswerGet responseAnswer = mapper.readValue(response, AnswerGet.class);

		Assertions.assertEquals(answer.getId(), responseAnswer.getId());
		Assertions.assertEquals(answer.getText(), responseAnswer.getText());
		Assertions.assertEquals(answer.getUser().getUsername(), responseAnswer.getUser());
	}

	@ParameterizedTest(name = "{index} ''{2}''")
	@MethodSource("examplesOfNotFoundUrlsAndErrorMessages")
	void getAnswerByQuestionIdAndAnswerId_questionOrAnswerNotExists_notFound(String url, String expectedErrorMessage,
																			 String nameOfTestCase) throws Exception {

		String token = prepareToken("user", "password");

		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						get(url)
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	private static Stream<Arguments> examplesOfNotFoundUrlsAndErrorMessages() {
		return Stream.of(
				Arguments.of("/api/questions/2000/answers/1000", "Answer not found with questionId: 2000 and answerId: 1000",
						"questionNotExists_notFound"),
				Arguments.of("/api/questions/1000/answers/2000", "Answer not found with questionId: 1000 and answerId: 2000",
						"answerNotExists_notFound"),
				Arguments.of("/api/questions/2000/answers/2000", "Answer not found with questionId: 2000 and answerId: 2000",
						"answerAndQuestionNotExists_notFound")
		);
	}

	@Test
	void addAnswer_withAnonymousUser_unauthorized() throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		mockMvc.perform(
						post("/api/questions/1000/answers")
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isUnauthorized());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void addAnswer_questionNotExists_notFound() throws Exception {

		String token = prepareToken("user", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						post("/api/questions/2000/answers")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Question not found with id: 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void addAnswer_simpleCase_success() throws Exception {

		String token = prepareToken("user", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		User user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(answerRepository.save(any(Answer.class))).willReturn(new Answer(answerToRequest.getText(), user));

		String response = mockMvc.perform(
						post("/api/questions/1000/answers")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		AnswerGet responseAnswer = mapper.readValue(response, AnswerGet.class);
		Assertions.assertEquals(answerToRequest.getText(), responseAnswer.getText());

		verify(mailService, times(1)).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, times(1)).save(any(Answer.class));
	}

	@Test
	void addAnswer_tooShortText_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareAnswerWithTooShortTextToRequest();
		mockMvc.perform(
						post("/api/questions/1000/answers")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void addAnswer_tooShortTextAfterTrim_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareAnswerWithTooShortTextAfterTrimToRequest();
		mockMvc.perform(
						post("/api/questions/1000/answers")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void addAnswer_emptyText_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareAnswerWithEmptyTextToRequest();
		mockMvc.perform(
						post("/api/questions/1000/answers")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void addAnswer_nullText_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareAnswerWithNullTextToRequest();
		mockMvc.perform(
						post("/api/questions/1000/answers")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void addAnswer_emptyBody_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(
						post("/api/questions/1000/answers")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content("")
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void updateAnswer_withAnonymousUser_unauthorized() throws Exception {
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();

		mockMvc.perform(
						put("/api/questions/1000/answers/1000")
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isUnauthorized());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void updateAnswer_userUpdatesHisOwnAnswer_success() throws Exception {

		String token = prepareToken("user2", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		User user = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		given(answerRepository.save(any(Answer.class))).willReturn(new Answer(answerToRequest.getText(), user));

		String response = mockMvc.perform(
						put("/api/questions/1000/answers/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		AnswerGet responseAnswer = mapper.readValue(response, AnswerGet.class);
		Assertions.assertEquals(answerToRequest.getText(), responseAnswer.getText());

		verify(mailService, times(1)).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, times(1)).save(any(Answer.class));
	}

	@Test
	void updateAnswer_tooShortText_badRequest() throws Exception {

		String token = prepareToken("user2", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareAnswerWithTooShortTextToRequest();
		mockMvc.perform(
						put("/api/questions/1000/answers/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void updateAnswer_emptyText_badRequest() throws Exception {

		String token = prepareToken("user2", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareAnswerWithEmptyTextToRequest();
		mockMvc.perform(
						put("/api/questions/1000/answers/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void updateAnswer_nullText_badRequest() throws Exception {

		String token = prepareToken("user2", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareAnswerWithNullTextToRequest();
		mockMvc.perform(
						put("/api/questions/1000/answers/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void updateAnswer_emptyBody_badRequest() throws Exception {

		String token = prepareToken("user2", "password");

		mockMvc.perform(
						put("/api/questions/1000/answers/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content("")
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void updateAnswer_administratorUpdatesAnotherUsersAnswer_success() throws Exception {

		String token = prepareToken("administrator", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		User user = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		given(answerRepository.save(any(Answer.class))).willReturn(new Answer(answerToRequest.getText(), user));

		String response = mockMvc.perform(
						put("/api/questions/1000/answers/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		AnswerGet responseAnswer = mapper.readValue(response, AnswerGet.class);
		Assertions.assertEquals(answerToRequest.getText(), responseAnswer.getText());

		verify(mailService, times(1)).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, times(1)).save(any(Answer.class));
	}

	@Test
	void updateAnswer_userUpdatesAnotherUsersAnswer_forbidden() throws Exception {

		String token = prepareToken("user", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						put("/api/questions/1000/answers/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isForbidden())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Change not allowed!";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@ParameterizedTest(name = "{index} ''{2}''")
	@MethodSource("examplesOfNotFoundUrlsAndErrorMessages")
	void updateAnswer_questionOrAnswerNotExists_notFound(String url, String expectedErrorMessage,
														 String nameOfTestCase) throws Exception {

		String token = prepareToken("user2", "password");

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						put(url)
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(Answer.class));
	}

	@Test
	void deleteAnswer_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						delete("/api/questions/1000/answers/1000"))
				.andExpect(status().isUnauthorized());

		verify(answerRepository, never()).delete(any(Answer.class));
	}

	@Test
	void deleteAnswer_userDeletesHisOwnAnswer_success() throws Exception {

		String token = prepareToken("user2", "password");

		String response = mockMvc.perform(
						delete("/api/questions/1000/answers/1000")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Assertions.assertEquals("true", response);

		verify(answerRepository, times(1)).delete(any(Answer.class));
	}

	@Test
	void deleteAnswer_administratorDeletesAnotherUsersAnswer_success() throws Exception {

		String token = prepareToken("administrator", "password");

		String response = mockMvc.perform(
						delete("/api/questions/1000/answers/1000")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Assertions.assertEquals("true", response);

		verify(answerRepository, times(1)).delete(any(Answer.class));
	}

	@Test
	void deleteAnswer_userDeletesAnotherUsersAnswer_forbidden() throws Exception {

		String token = prepareToken("user", "password");

		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						delete("/api/questions/1000/answers/1000")
								.header("Authorization", token))
				.andExpect(status().isForbidden())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Change not allowed!";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(answerRepository, never()).delete(any(Answer.class));
	}

	@ParameterizedTest(name = "{index} ''{2}''")
	@MethodSource("examplesOfNotFoundUrlsAndErrorMessages")
	void deleteAnswer_questionOrAnswerNotExists_notFound(String url, String expectedErrorMessage,
														 String nameOfTestCase) throws Exception {

		String token = prepareToken("user2", "password");

		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						delete(url)
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(answerRepository, never()).delete(any(Answer.class));
	}

	@Test
	void downloadPdf_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						get("/api/questions/1000/answers/pdf-export"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void downloadPdf_simpleCase_success() throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(
						get("/api/questions/1000/answers/pdf-export")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"));
	}

	@Test
	void downloadExcel_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						get("/api/questions/1000/answers/excel-export"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void downloadExcel_simpleCase_success() throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(
						get("/api/questions/1000/answers/excel-export")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"));
	}

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("examplesOfQuestionNotFoundUrls")
	void downloadFile_questionNotExists_notFound(String url, String nameOfTestCase) throws Exception {

		String token = prepareToken("user", "password");

		String receivedErrorMessage = mockMvc.perform(
						get(url)
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

		String expectedErrorMessage = "Question not found with id: 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	private static Stream<Arguments> examplesOfQuestionNotFoundUrls() {
		return Stream.of(
				Arguments.of("/api/questions/2000/answers/pdf-export",
						"downloadPdf_questionNotExists_notFound"),
				Arguments.of("/api/questions/2000/answers/excel-export",
						"downloadExcel_questionNotExists_notFound")
		);
	}

	private String prepareToken(String username, String password) throws Exception {
		return mockMvc.perform(post("/api/login")
						.content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}"))
				.andExpect(status().isOk())
				.andExpect(header().exists("Authorization"))
				.andReturn().getResponse().getHeader("Authorization");
	}
}
