package pl.marcinm312.springquestionsanswers.answer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springquestionsanswers.answer.model.AnswerEntity;
import pl.marcinm312.springquestionsanswers.answer.model.dto.AnswerCreateUpdate;
import pl.marcinm312.springquestionsanswers.answer.model.dto.AnswerGet;
import pl.marcinm312.springquestionsanswers.answer.repository.AnswerRepository;
import pl.marcinm312.springquestionsanswers.answer.service.AnswerManager;
import pl.marcinm312.springquestionsanswers.answer.testdataprovider.AnswerDataProvider;
import pl.marcinm312.springquestionsanswers.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springquestionsanswers.config.security.SecurityMessagesConfig;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springquestionsanswers.config.security.utils.SessionUtils;
import pl.marcinm312.springquestionsanswers.question.model.QuestionEntity;
import pl.marcinm312.springquestionsanswers.question.repository.QuestionRepository;
import pl.marcinm312.springquestionsanswers.question.service.QuestionManager;
import pl.marcinm312.springquestionsanswers.question.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springquestionsanswers.shared.file.ExcelGenerator;
import pl.marcinm312.springquestionsanswers.shared.file.PdfGenerator;
import pl.marcinm312.springquestionsanswers.shared.filter.Filter;
import pl.marcinm312.springquestionsanswers.shared.mail.MailService;
import pl.marcinm312.springquestionsanswers.shared.testdataprovider.JwtProvider;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.repository.ActivationTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.MailChangeTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springquestionsanswers.user.service.UserManager;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;

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
@MockBeans({@MockBean(ActivationTokenRepo.class), @MockBean(MailChangeTokenRepo.class), @MockBean(SessionUtils.class)})
@SpyBeans({@SpyBean(QuestionManager.class), @SpyBean(AnswerManager.class), @SpyBean(UserDetailsServiceImpl.class),
		@SpyBean(UserManager.class), @SpyBean(RestAuthenticationSuccessHandler.class),
		@SpyBean(RestAuthenticationFailureHandler.class), @SpyBean(ExcelGenerator.class), @SpyBean(PdfGenerator.class)})
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

	private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

	private final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final UserEntity secondUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
	private final UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();

	private final QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();

	@BeforeEach
	void setup() {

		given(questionRepository.existsById(1000L)).willReturn(true);
		given(questionRepository.existsById(2000L)).willReturn(false);

		given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
		given(questionRepository.findById(2000L)).willReturn(Optional.empty());
		
		given(answerRepository.getPaginatedAnswers(1000L, PageRequest.of(0, 5,
				Sort.by(Sort.Direction.DESC, "id"))))
				.willReturn(new PageImpl<>(AnswerDataProvider.prepareExampleAnswersList()));
		given(answerRepository.getPaginatedAnswers(1000L, PageRequest.of(0, 5000,
				Sort.by(Sort.Direction.DESC, "id"))))
				.willReturn(new PageImpl<>(AnswerDataProvider.prepareExampleAnswersList()));
		given(answerRepository.searchPaginatedAnswers(1000L, "answer1",
				PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"))))
				.willReturn(new PageImpl<>(AnswerDataProvider.prepareExampleSearchedAnswersList()));
		given(answerRepository.findByQuestionIdAndId(1000L, 1000L))
				.willReturn(Optional.of(AnswerDataProvider.prepareExampleAnswer()));
		given(answerRepository.findByQuestionIdAndId(1000L, 2000L)).willReturn(Optional.empty());
		given(answerRepository.findByQuestionIdAndId(2000L, 1000L)).willReturn(Optional.empty());
		given(answerRepository.findByQuestionIdAndId(2000L, 2000L)).willReturn(Optional.empty());

		given(userRepo.findById(commonUser.getId())).willReturn(Optional.of(commonUser));
		given(userRepo.findById(secondUser.getId())).willReturn(Optional.of(secondUser));
		given(userRepo.findById(adminUser.getId())).willReturn(Optional.of(adminUser));

		given(userRepo.findByUsername(commonUser.getUsername())).willReturn(Optional.of(commonUser));
		given(userRepo.findByUsername(secondUser.getUsername())).willReturn(Optional.of(secondUser));
		given(userRepo.findByUsername(adminUser.getUsername())).willReturn(Optional.of(adminUser));

		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(this.webApplicationContext)
						.apply(springSecurity())
						.alwaysDo(print())
						.build();
	}

	@Test
	void getAnswers_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(
						get("/api/questions/1000/answers"))
				.andExpect(status().isUnauthorized());
	}

	@ParameterizedTest
	@MethodSource("examplesOfAnswersGetUrls")
	void getAnswers_parameterized_success(String url, int expectedElements) throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		String response = mockMvc.perform(get(url).header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		ObjectNode root = (ObjectNode) mapper.readTree(response);
		int amountOfElements = root.get("itemsList").size();
		Assertions.assertEquals(expectedElements, amountOfElements);
	}

	private static Stream<Arguments> examplesOfAnswersGetUrls() {

		return Stream.of(
				Arguments.of("/api/questions/1000/answers", 3),
				Arguments.of("/api/questions/1000/answers?keyword=answer1&pageNo=-1&pageSize=0&sortField=TITLE&sortDirection=ASC", 1),
				Arguments.of("/api/questions/1000/answers?keyword=answer1&pageNo=1&pageSize=0&sortField=DESCRIPTION&sortDirection=ASC", 1),
				Arguments.of("/api/questions/1000/answers?keyword=answer1&pageNo=0&pageSize=5&sortField=TITLE&sortDirection=ASC", 1),
				Arguments.of("/api/questions/1000/answers?keyword=answer1&pageNo=1&pageSize=5&sortField=TITLE&sortDirection=ASC", 1),
				Arguments.of("/api/questions/1000/answers?keyword=answer1&pageNo=1&pageSize=5&sortField=ID&sortDirection=ASC", 1),
				Arguments.of("/api/questions/1000/answers?pageNo=1&pageSize=5&sortField=TITLE&sortDirection=DESC", 3),
				Arguments.of("/api/questions/1000/answers?pageNo=1&pageSize=5000&sortField=TITLE&sortDirection=DESC", 3)
		);
	}

	@ParameterizedTest
	@MethodSource("examplesOfTooLargePageSizeUrls")
	void limitExceeded_tooLargePageSize_badRequest(String url) throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		String receivedErrorMessage = Objects.requireNonNull(
				mockMvc.perform(get(url).header("Authorization", token))
						.andExpect(status().isBadRequest())
						.andReturn().getResolvedException()).getMessage();

		int rowsLimit = Filter.ROWS_LIMIT;
		String expectedErrorMessage = "Strona nie może zawierać więcej niż " + rowsLimit + " rekordów";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	private static Stream<Arguments> examplesOfTooLargePageSizeUrls() {

		return Stream.of(
				Arguments.of("/api/questions/1000/answers?pageNo=1&pageSize=5001&sortField=TITLE&sortDirection=DESC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=PDF&pageNo=1&pageSize=5001&sortField=TITLE&sortDirection=DESC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=EXCEL&pageNo=1&pageSize=5001&sortField=TITLE&sortDirection=DESC")
		);
	}

	@Test
	void getAnswers_questionNotExists_notFound() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						get("/api/questions/2000/answers")
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
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

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		String response = mockMvc.perform(
						get("/api/questions/1000/answers/1000")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		AnswerGet responseAnswer = mapper.readValue(response, AnswerGet.class);
		AnswerEntity answer = AnswerDataProvider.prepareExampleAnswer();
		Assertions.assertEquals(answer.getId(), responseAnswer.getId());
		Assertions.assertEquals(answer.getText(), responseAnswer.getText());
		Assertions.assertEquals(answer.getUser().getUsername(), responseAnswer.getUser());
	}

	@ParameterizedTest
	@MethodSource("examplesOfNotFoundUrlsAndErrorMessages")
	void getAnswerByQuestionIdAndAnswerId_questionOrAnswerNotExists_notFound(String url, String expectedErrorMessage)
			throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						get(url)
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	private static Stream<Arguments> examplesOfNotFoundUrlsAndErrorMessages() {

		return Stream.of(
				Arguments.of("/api/questions/2000/answers/1000", "Nie znaleziono odpowiedzi o id: 1000 na pytanie o id: 2000"),
				Arguments.of("/api/questions/1000/answers/2000", "Nie znaleziono odpowiedzi o id: 2000 na pytanie o id: 1000"),
				Arguments.of("/api/questions/2000/answers/2000", "Nie znaleziono odpowiedzi o id: 2000 na pytanie o id: 2000")
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
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	void addAnswer_questionNotExists_notFound() throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						post("/api/questions/2000/answers")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	void addAnswer_simpleCase_success() throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(answerRepository.save(any(AnswerEntity.class))).willReturn(new AnswerEntity(answerToRequest.getText(), question, user));
		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
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
		verify(answerRepository, times(1)).save(any(AnswerEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfAddAnswerBadRequests")
	void addAnswer_incorrectAnswer_badRequest(AnswerCreateUpdate answerToRequest) throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(
						post("/api/questions/1000/answers")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	private static Stream<Arguments> examplesOfAddAnswerBadRequests() {

		return Stream.of(
				Arguments.of(AnswerDataProvider.prepareAnswerWithTooShortTextToRequest()),
				Arguments.of(AnswerDataProvider.prepareAnswerWithTooShortTextAfterTrimToRequest()),
				Arguments.of(AnswerDataProvider.prepareAnswerWithEmptyTextToRequest()),
				Arguments.of(AnswerDataProvider.prepareAnswerWithNullTextToRequest())
		);
	}

	@Test
	void addAnswer_emptyBody_badRequest() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(
						post("/api/questions/1000/answers")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content("")
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
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
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfSuccessfullyUpdateOrDeleteAnswer")
	void updateAnswer_userUpdatesAnswer_success(UserEntity loggedUser) throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		UserEntity user = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
		given(answerRepository.save(any(AnswerEntity.class))).willReturn(new AnswerEntity(answerToRequest.getText(), question, user));
		given(userRepo.getUserFromAuthentication(any())).willReturn(loggedUser);

		String token = new JwtProvider(mockMvc).prepareToken(loggedUser.getUsername(), "password");
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
		verify(answerRepository, times(1)).save(any(AnswerEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfUpdateAnswerBadRequests")
	void updateAnswer_incorrectAnswer_badRequest(AnswerCreateUpdate answerToRequest) throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user2", "password");
		mockMvc.perform(
						put("/api/questions/1000/answers/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	private static Stream<Arguments> examplesOfUpdateAnswerBadRequests() {

		return Stream.of(
				Arguments.of(AnswerDataProvider.prepareAnswerWithTooShortTextToRequest()),
				Arguments.of(AnswerDataProvider.prepareAnswerWithTooShortTextAfterTrimToRequest()),
				Arguments.of(AnswerDataProvider.prepareAnswerWithEmptyTextToRequest()),
				Arguments.of(AnswerDataProvider.prepareAnswerWithNullTextToRequest())
		);
	}

	@Test
	void updateAnswer_emptyBody_badRequest() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user2", "password");
		mockMvc.perform(
						put("/api/questions/1000/answers/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content("")
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	void updateAnswer_userUpdatesAnotherUsersAnswer_forbidden() throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						put("/api/questions/1000/answers/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(answerToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isForbidden())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Brak uprawnień do wykonania operacji!";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfNotFoundUrlsAndErrorMessages")
	void updateAnswer_questionOrAnswerNotExists_notFound(String url, String expectedErrorMessage) throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		given(userRepo.getUserFromAuthentication(any())).willReturn(secondUser);

		String token = new JwtProvider(mockMvc).prepareToken("user2", "password");
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
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	void deleteAnswer_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(
						delete("/api/questions/1000/answers/1000"))
				.andExpect(status().isUnauthorized());

		verify(answerRepository, never()).delete(any(AnswerEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfSuccessfullyUpdateOrDeleteAnswer")
	void deleteAnswer_userDeletesAnswer_success(UserEntity loggedUser) throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(loggedUser);

		String token = new JwtProvider(mockMvc).prepareToken(loggedUser.getUsername(), "password");
		String response = mockMvc.perform(
						delete("/api/questions/1000/answers/1000")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Assertions.assertEquals("true", response);
		verify(answerRepository, times(1)).delete(any(AnswerEntity.class));
	}

	private static Stream<Arguments> examplesOfSuccessfullyUpdateOrDeleteAnswer() {

		return Stream.of(
				Arguments.of(UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword()),
				Arguments.of(UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword())
		);
	}

	@Test
	void deleteAnswer_userDeletesAnotherUsersAnswer_forbidden() throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						delete("/api/questions/1000/answers/1000")
								.header("Authorization", token))
				.andExpect(status().isForbidden())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Brak uprawnień do wykonania operacji!";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
		verify(answerRepository, never()).delete(any(AnswerEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfNotFoundUrlsAndErrorMessages")
	void deleteAnswer_questionOrAnswerNotExists_notFound(String url, String expectedErrorMessage) throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(secondUser);

		String token = new JwtProvider(mockMvc).prepareToken("user2", "password");
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						delete(url)
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
		verify(answerRepository, never()).delete(any(AnswerEntity.class));
	}

	@Test
	void downloadPdf_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(
						get("/api/questions/1000/answers/file-export?fileType=PDF"))
				.andExpect(status().isUnauthorized());
	}

	@ParameterizedTest
	@MethodSource("examplesOfDownloadPdfUrls")
	void downloadPdf_parameterized_success(String url) throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(get(url).header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"));
	}

	private static Stream<Arguments> examplesOfDownloadPdfUrls() {

		return Stream.of(
				Arguments.of("/api/questions/1000/answers/file-export?fileType=PDF"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=PDF&keyword=answer1&pageNo=-1&pageSize=0&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=PDF&keyword=answer1&pageNo=1&pageSize=0&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=PDF&keyword=answer1&pageNo=0&pageSize=5&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=PDF&keyword=answer1&pageNo=1&pageSize=5&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=PDF&keyword=answer1&pageNo=1&pageSize=5&sortField=ID&sortDirection=ASC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=PDF&pageNo=1&pageSize=5&sortField=TITLE&sortDirection=DESC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=PDF&pageNo=1&pageSize=5000&sortField=TITLE&sortDirection=DESC")
		);
	}

	@Test
	void downloadExcel_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(
						get("/api/questions/1000/answers/file-export?fileType=EXCEL"))
				.andExpect(status().isUnauthorized());
	}

	@ParameterizedTest
	@MethodSource("examplesOfDownloadExcelUrls")
	void downloadExcel_parameterized_success(String url) throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(get(url).header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"));
	}

	private static Stream<Arguments> examplesOfDownloadExcelUrls() {

		return Stream.of(
				Arguments.of("/api/questions/1000/answers/file-export?fileType=EXCEL"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=EXCEL&keyword=answer1&pageNo=-1&pageSize=0&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=EXCEL&keyword=answer1&pageNo=1&pageSize=0&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=EXCEL&keyword=answer1&pageNo=0&pageSize=5&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=EXCEL&keyword=answer1&pageNo=1&pageSize=5&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=EXCEL&keyword=answer1&pageNo=1&pageSize=5&sortField=ID&sortDirection=ASC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=EXCEL&pageNo=1&pageSize=5&sortField=TITLE&sortDirection=DESC"),
				Arguments.of("/api/questions/1000/answers/file-export?fileType=EXCEL&pageNo=1&pageSize=5000&sortField=TITLE&sortDirection=DESC")
		);
	}

	@ParameterizedTest
	@MethodSource("examplesOfQuestionNotFoundUrls")
	void downloadFile_questionNotExists_notFound(String url) throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						get(url)
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	private static Stream<Arguments> examplesOfQuestionNotFoundUrls() {

		return Stream.of(
				Arguments.of("/api/questions/2000/answers/file-export?fileType=PDF"),
				Arguments.of("/api/questions/2000/answers/file-export?fileType=EXCEL")
		);
	}
}
