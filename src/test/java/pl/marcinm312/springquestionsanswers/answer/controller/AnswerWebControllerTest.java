package pl.marcinm312.springquestionsanswers.answer.controller;

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
import org.springframework.web.servlet.ModelAndView;
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
import pl.marcinm312.springquestionsanswers.question.model.dto.QuestionGet;
import pl.marcinm312.springquestionsanswers.question.repository.QuestionRepository;
import pl.marcinm312.springquestionsanswers.question.service.QuestionManager;
import pl.marcinm312.springquestionsanswers.question.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springquestionsanswers.shared.file.ExcelGenerator;
import pl.marcinm312.springquestionsanswers.shared.file.PdfGenerator;
import pl.marcinm312.springquestionsanswers.shared.filter.Filter;
import pl.marcinm312.springquestionsanswers.shared.mail.MailService;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.repository.ActivationTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.MailChangeTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springquestionsanswers.user.service.UserManager;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AnswerWebController.class)
@ComponentScan(basePackageClasses = AnswerWebController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = AnswerWebController.class)
		})
@MockBeans({@MockBean(ActivationTokenRepo.class), @MockBean(MailChangeTokenRepo.class), @MockBean(SessionUtils.class)})
@SpyBeans({@SpyBean(QuestionManager.class), @SpyBean(AnswerManager.class), @SpyBean(UserDetailsServiceImpl.class),
		@SpyBean(UserManager.class), @SpyBean(RestAuthenticationSuccessHandler.class),
		@SpyBean(RestAuthenticationFailureHandler.class), @SpyBean(ExcelGenerator.class), @SpyBean(PdfGenerator.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
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

	private static final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private static final UserEntity secondUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
	private static final UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();

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

		given(userRepo.findByUsername("user")).willReturn(Optional.of(commonUser));
		given(userRepo.findByUsername("user2")).willReturn(Optional.of(secondUser));
		given(userRepo.findByUsername("admin")).willReturn(Optional.of(adminUser));

		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(this.webApplicationContext)
						.apply(springSecurity())
						.alwaysDo(print())
						.build();
	}

	@Test
	void answersGet_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/questions/1000/answers/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@ParameterizedTest
	@MethodSource("examplesOfAnswersGetUrls")
	void answersGet_parameterized_success(String url, int expectedElements) throws Exception {

		ModelAndView modelAndView = mockMvc.perform(get(url).with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("answers"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		List<AnswerGet> answersFromModel = (List<AnswerGet>) modelAndView.getModel().get("answerList");
		int arrayResultSize = answersFromModel.size();
		Assertions.assertEquals(expectedElements, arrayResultSize);

		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());
	}

	private static Stream<Arguments> examplesOfAnswersGetUrls() {

		return Stream.of(
				Arguments.of("/app/questions/1000/answers/", 3),
				Arguments.of("/app/questions/1000/answers/?keyword=answer1&pageNo=-1&pageSize=0&sortField=TITLE&sortDirection=ASC", 1),
				Arguments.of("/app/questions/1000/answers/?keyword=answer1&pageNo=1&pageSize=0&sortField=TITLE&sortDirection=ASC", 1),
				Arguments.of("/app/questions/1000/answers/?keyword=answer1&pageNo=0&pageSize=5&sortField=TITLE&sortDirection=ASC", 1),
				Arguments.of("/app/questions/1000/answers/?keyword=answer1&pageNo=1&pageSize=5&sortField=TITLE&sortDirection=ASC", 1),
				Arguments.of("/app/questions/1000/answers/?keyword=answer1&pageNo=1&pageSize=5&sortField=ID&sortDirection=ASC", 1),
				Arguments.of("/app/questions/1000/answers/?pageNo=1&pageSize=5&sortField=TITLE&sortDirection=DESC", 3),
				Arguments.of("/app/questions/1000/answers/?pageNo=1&pageSize=5000&sortField=TITLE&sortDirection=DESC", 3)
		);
	}

	@Test
	void answersGet_tooLargePageSize_badRequest() throws Exception {

		String url = "/app/questions/1000/answers/?pageNo=1&pageSize=5001&sortField=TITLE&sortDirection=DESC";
		ModelAndView modelAndView = mockMvc.perform(
						get(url).with(user("user").password("password")))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("limitExceeded"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		String receivedErrorMessage = (String) modelAndView.getModel().get("message");
		int rowsLimit = Filter.ROWS_LIMIT;
		String expectedErrorMessage = "Strona nie może zawierać więcej niż " + rowsLimit + " rekordów";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	@ParameterizedTest
	@MethodSource("examplesOfTooLargePageSizeUrls")
	void fileExport_tooLargePageSize_badRequest(String url) throws Exception {

		String receivedErrorMessage = Objects.requireNonNull(
				mockMvc.perform(get(url).with(user("user").password("password")))
						.andExpect(status().isBadRequest())
						.andReturn().getResolvedException()).getMessage();

		int rowsLimit = Filter.ROWS_LIMIT;
		String expectedErrorMessage = "Strona nie może zawierać więcej niż " + rowsLimit + " rekordów";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	private static Stream<Arguments> examplesOfTooLargePageSizeUrls() {

		return Stream.of(
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=PDF&pageNo=1&pageSize=5001&sortField=TITLE&sortDirection=DESC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=EXCEL&pageNo=1&pageSize=5001&sortField=TITLE&sortDirection=DESC")
		);
	}

	@Test
	void answersGet_questionNotExists_notFoundMessage() throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/2000/answers/")
								.with(user("user").password("password")))
				.andExpect(status().isNotFound())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		String messageFromModel = (String) modelAndView.getModel().get("message");
		String expectedMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedMessage, messageFromModel);
	}

	@Test
	void createAnswer_withAnonymousUser_redirectToLoginPage() throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		mockMvc.perform(
						post("/app/questions/1000/answers/new/")
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	void createAnswer_withoutCsrfToken_forbidden() throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		mockMvc.perform(
						post("/app/questions/1000/answers/new/")
								.with(user("user2").password("password"))
								.param("text", answerToRequest.getText()))
				.andExpect(status().isForbidden());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	void createAnswer_withCsrfInvalidToken_forbidden() throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		mockMvc.perform(
						post("/app/questions/1000/answers/new/")
								.with(user("user2").password("password"))
								.with(csrf().useInvalidToken())
								.param("text", answerToRequest.getText()))
				.andExpect(status().isForbidden());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	void createAnswer_simpleCase_success() throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(answerRepository.save(any(AnswerEntity.class))).willReturn(new AnswerEntity(answerToRequest.getText(), question, user));
		given(userRepo.getUserFromAuthentication(any())).willReturn(secondUser);

		mockMvc.perform(
						post("/app/questions/1000/answers/new/")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(".."))
				.andExpect(view().name("redirect:.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("user2").withRoles("USER"));

		verify(mailService, times(1)).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, times(1)).save(any(AnswerEntity.class));
	}

	@Test
	void createAnswer_questionNotExists_notFoundMessage() throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		given(userRepo.getUserFromAuthentication(any())).willReturn(secondUser);

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/2000/answers/new/")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().isNotFound())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().attributeExists("userLogin", "answer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		String messageFromModel = (String) modelAndView.getModel().get("message");
		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfCreateUpdateAnswerBadRequests")
	void createAnswer_incorrectAnswer_validationErrors(AnswerCreateUpdate answerToRequest, String expectedText) throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/1000/answers/new/")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("createAnswer"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("answer", "text"))
				.andExpect(model().attributeExists("question", "userLogin", "answer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());

		AnswerCreateUpdate answerFromModel = (AnswerCreateUpdate) modelAndView.getModel().get("answer");
		Assertions.assertEquals(expectedText, answerFromModel.getText());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	private static Stream<Arguments> examplesOfCreateUpdateAnswerBadRequests() {

		AnswerCreateUpdate answerWithTooShortText = AnswerDataProvider.prepareAnswerWithTooShortTextToRequest();
		AnswerCreateUpdate answerWithTooShortTextAfterTrim = AnswerDataProvider.prepareAnswerWithTooShortTextAfterTrimToRequest();
		AnswerCreateUpdate answerWithEmptyText = AnswerDataProvider.prepareAnswerWithEmptyTextToRequest();
		return Stream.of(
				Arguments.of(answerWithTooShortText, answerWithTooShortText.getText()),
				Arguments.of(answerWithTooShortTextAfterTrim, answerWithTooShortTextAfterTrim.getText().trim()),
				Arguments.of(answerWithEmptyText, null)
		);
	}

	@Test
	void createAnswerView_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/questions/1000/answers/new/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@Test
	void createAnswerView_simpleCase_success() throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/1000/answers/new/")
								.with(user("user2").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("createAnswer"))
				.andExpect(model().attributeExists("question", "userLogin", "answer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());
	}

	@Test
	void createAnswerView_questionNotExists_notFoundMessage() throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/2000/answers/new/")
								.with(user("user2").password("password")))
				.andExpect(status().isNotFound())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().attributeExists("message", "userLogin"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		String messageFromModel = (String) modelAndView.getModel().get("message");
		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
	}

	@Test
	void editAnswer_withAnonymousUser_redirectToLoginPage() throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit/")
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	void editAnswer_withoutCsrfToken_forbidden() throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit/")
								.with(user("user2").password("password"))
								.param("text", answerToRequest.getText()))
				.andExpect(status().isForbidden());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	void editAnswer_withCsrfInvalidToken_forbidden() throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit/")
								.with(user("user2").password("password"))
								.with(csrf().useInvalidToken())
								.param("text", answerToRequest.getText()))
				.andExpect(status().isForbidden());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfSuccessfullyUpdateOrDeleteAnswer")
	void editAnswer_userUpdatesAnswer_success(UserEntity loggedUser) throws Exception {

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(answerRepository.save(any(AnswerEntity.class))).willReturn(new AnswerEntity(answerToRequest.getText(), question, user));
		given(userRepo.getUserFromAuthentication(any())).willReturn(loggedUser);

		String role = loggedUser.getRole().name().replace("ROLE_", "");
		mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit/")
								.with(user(loggedUser.getUsername()).password("password").roles(role))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername(loggedUser.getUsername()).withRoles(role));

		verify(mailService, times(1)).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, times(1)).save(any(AnswerEntity.class));
	}

	private static Stream<Arguments> examplesOfSuccessfullyUpdateOrDeleteAnswer() {
		return Stream.of(
				Arguments.of(secondUser),
				Arguments.of(adminUser)
		);
	}

	@ParameterizedTest
	@MethodSource("examplesOfCreateUpdateAnswerBadRequests")
	void editAnswer_incorrectAnswer_validationErrors(AnswerCreateUpdate answerToRequest, String expectedText) throws Exception {

		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(answerRepository.save(any(AnswerEntity.class))).willReturn(new AnswerEntity(answerToRequest.getText(), question, user));

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit/")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("editAnswer"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("answer", "text"))
				.andExpect(model().attributeExists("question", "userLogin", "answer", "oldAnswer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());

		AnswerCreateUpdate answerFromModel = (AnswerCreateUpdate) modelAndView.getModel().get("answer");
		Assertions.assertEquals(expectedText, answerFromModel.getText());

		AnswerEntity expectedOldAnswer = AnswerDataProvider.prepareExampleAnswer();
		AnswerGet oldAnswerFromModel = (AnswerGet) modelAndView.getModel().get("oldAnswer");
		Assertions.assertEquals(expectedOldAnswer.getId(), oldAnswerFromModel.getId());
		Assertions.assertEquals(expectedOldAnswer.getText(), oldAnswerFromModel.getText());
		Assertions.assertEquals(expectedOldAnswer.getUser().getUsername(), oldAnswerFromModel.getUser());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	void editAnswer_userUpdatesAnotherUsersAnswer_changeNotAllowed() throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit/")
								.with(user("user").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().isForbidden())
				.andExpect(view().name("changeNotAllowed"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfNotFoundUrlsAndErrorMessages")
	void editAnswer_questionOrAnswerNotExists_notFoundMessage(String url, String expectedErrorMessage) throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);

		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		ModelAndView modelAndView = mockMvc.perform(
						post(url + "edit/")
								.with(user("user").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().isNotFound())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		String messageFromModel = (String) modelAndView.getModel().get("message");
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	void editAnswerView_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/questions/1000/answers/1000/edit/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@Test
	void editAnswerView_simpleCase_success() throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/1000/answers/1000/edit/")
								.with(user("user2").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("editAnswer"))
				.andExpect(model().attributeExists("question", "userLogin", "answer", "oldAnswer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());

		AnswerEntity expectedAnswer = AnswerDataProvider.prepareExampleAnswer();
		AnswerGet answerFromModel = (AnswerGet) modelAndView.getModel().get("answer");
		Assertions.assertEquals(expectedAnswer.getId(), answerFromModel.getId());
		Assertions.assertEquals(expectedAnswer.getText(), answerFromModel.getText());
		Assertions.assertEquals(expectedAnswer.getUser().getUsername(), answerFromModel.getUser());
		AnswerGet oldAnswerFromModel = (AnswerGet) modelAndView.getModel().get("oldAnswer");
		Assertions.assertEquals(expectedAnswer.getId(), oldAnswerFromModel.getId());
		Assertions.assertEquals(expectedAnswer.getText(), oldAnswerFromModel.getText());
		Assertions.assertEquals(expectedAnswer.getUser().getUsername(), oldAnswerFromModel.getUser());
	}

	@ParameterizedTest
	@MethodSource("examplesOfNotFoundUrlsAndErrorMessages")
	void editAnswerView_questionOrAnswerNotExists_notFoundMessage(String url, String expectedErrorMessage) throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get(url + "edit/")
								.with(user("user2").password("password")))
				.andExpect(status().isNotFound())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().attributeExists("message", "userLogin"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		String messageFromModel = (String) modelAndView.getModel().get("message");
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
	}

	@Test
	void removeAnswer_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						post("/app/questions/1000/answers/1000/delete/")
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());

		verify(answerRepository, never()).delete(any(AnswerEntity.class));
	}

	@Test
	void removeAnswer_withoutCsrfToken_forbidden() throws Exception {

		mockMvc.perform(
						post("/app/questions/1000/answers/1000/delete/")
								.with(user("user").password("password")))
				.andExpect(status().isForbidden());

		verify(answerRepository, never()).delete(any(AnswerEntity.class));
	}

	@Test
	void removeAnswer_withCsrfInvalidToken_forbidden() throws Exception {

		mockMvc.perform(
						post("/app/questions/1000/answers/1000/delete/")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken()))
				.andExpect(status().isForbidden());

		verify(answerRepository, never()).delete(any(AnswerEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfSuccessfullyUpdateOrDeleteAnswer")
	void removeAnswer_userDeletesAnswer_success(UserEntity loggedUser) throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(loggedUser);

		String role = loggedUser.getRole().name().replace("ROLE_", "");
		mockMvc.perform(
			post("/app/questions/1000/answers/1000/delete/")
					.with(user(loggedUser.getUsername()).password("password").roles(role))
					.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername(loggedUser.getUsername()).withRoles(role));

		verify(answerRepository, times(1)).delete(any(AnswerEntity.class));
	}

	@Test
	void removeAnswer_userDeletesAnotherUsersAnswer_changeNotAllowed() throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);

		mockMvc.perform(
						post("/app/questions/1000/answers/1000/delete/")
								.with(user("user").password("password"))
								.with(csrf()))
				.andExpect(status().isForbidden())
				.andExpect(view().name("changeNotAllowed"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(answerRepository, never()).delete(any(AnswerEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfNotFoundUrlsAndErrorMessages")
	void removeAnswer_questionOrAnswerNotExists_notFoundMessage(String url, String expectedErrorMessage) throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);

		ModelAndView modelAndView = mockMvc.perform(
						post(url + "delete/")
								.with(user("user").password("password"))
								.with(csrf()))
				.andExpect(status().isNotFound())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		String messageFromModel = (String) modelAndView.getModel().get("message");
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
		verify(answerRepository, never()).delete(any(AnswerEntity.class));
	}

	@Test
	void removeAnswerView_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/questions/1000/answers/1000/delete/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@Test
	void removeAnswerView_simpleCase_success() throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
				get("/app/questions/1000/answers/1000/delete/")
						.with(user("user2").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("deleteAnswer"))
				.andExpect(model().attributeExists("question", "userLogin", "answer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());

		AnswerGet answerFromModel = (AnswerGet) modelAndView.getModel().get("answer");
		AnswerEntity expectedAnswer = AnswerDataProvider.prepareExampleAnswer();
		Assertions.assertEquals(expectedAnswer.getId(), answerFromModel.getId());
		Assertions.assertEquals(expectedAnswer.getText(), answerFromModel.getText());
		Assertions.assertEquals(expectedAnswer.getUser().getUsername(), answerFromModel.getUser());
	}

	@ParameterizedTest
	@MethodSource("examplesOfNotFoundUrlsAndErrorMessages")
	void removeAnswerView_questionOrAnswerNotExists_notFoundMessage(String url, String expectedErrorMessage) throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get(url + "delete/")
								.with(user("user2").password("password")))
				.andExpect(status().isNotFound())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().attributeExists("message", "userLogin"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		String messageFromModel = (String) modelAndView.getModel().get("message");
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
	}

	private static Stream<Arguments> examplesOfNotFoundUrlsAndErrorMessages() {

		return Stream.of(
				Arguments.of("/app/questions/2000/answers/1000/", "Nie znaleziono odpowiedzi o id: 1000 na pytanie o id: 2000"),
				Arguments.of("/app/questions/1000/answers/2000/", "Nie znaleziono odpowiedzi o id: 2000 na pytanie o id: 1000"),
				Arguments.of("/app/questions/2000/answers/2000/", "Nie znaleziono odpowiedzi o id: 2000 na pytanie o id: 2000")
		);
	}

	@Test
	void downloadPdf_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/questions/1000/answers/file-export/?fileType=PDF"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@ParameterizedTest
	@MethodSource("examplesOfDownloadPdfUrls")
	void downloadPdf_parameterized_success(String url) throws Exception {

		mockMvc.perform(get(url).with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	private static Stream<Arguments> examplesOfDownloadPdfUrls() {

		return Stream.of(
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=PDF"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=PDF&keyword=answer1&pageNo=-1&pageSize=0&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=PDF&keyword=answer1&pageNo=1&pageSize=0&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=PDF&keyword=answer1&pageNo=0&pageSize=5&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=PDF&keyword=answer1&pageNo=1&pageSize=5&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=PDF&keyword=answer1&pageNo=1&pageSize=5&sortField=ID&sortDirection=ASC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=PDF&pageNo=1&pageSize=5&sortField=TITLE&sortDirection=DESC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=PDF&pageNo=1&pageSize=5000&sortField=TITLE&sortDirection=DESC")
		);
	}

	@Test
	void downloadExcel_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/questions/1000/answers/file-export/?fileType=EXCEL"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@ParameterizedTest
	@MethodSource("examplesOfDownloadExcelUrls")
	void downloadExcel_parameterized_success(String url) throws Exception {

		mockMvc.perform(get(url).with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	private static Stream<Arguments> examplesOfDownloadExcelUrls() {

		return Stream.of(
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=EXCEL"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=EXCEL&keyword=answer1&pageNo=-1&pageSize=0&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=EXCEL&keyword=answer1&pageNo=1&pageSize=0&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=EXCEL&keyword=answer1&pageNo=0&pageSize=5&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=EXCEL&keyword=answer1&pageNo=1&pageSize=5&sortField=TITLE&sortDirection=ASC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=EXCEL&keyword=answer1&pageNo=1&pageSize=5&sortField=ID&sortDirection=ASC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=EXCEL&pageNo=1&pageSize=5&sortField=TITLE&sortDirection=DESC"),
				Arguments.of("/app/questions/1000/answers/file-export/?fileType=EXCEL&pageNo=1&pageSize=5000&sortField=TITLE&sortDirection=DESC")
		);
	}

	@ParameterizedTest
	@MethodSource("examplesOfQuestionNotFoundUrls")
	void downloadFile_questionNotExists_notFound(String url) throws Exception {

		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						get(url).with(user("user").password("password")))
				.andExpect(status().isNotFound())
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	private static Stream<Arguments> examplesOfQuestionNotFoundUrls() {

		return Stream.of(
				Arguments.of("/app/questions/2000/answers/file-export/?fileType=PDF"),
				Arguments.of("/app/questions/2000/answers/file-export/?fileType=EXCEL")
		);
	}
}
