package pl.marcinm312.springquestionsanswers.question.controller;

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
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import pl.marcinm312.springquestionsanswers.config.PropertiesConfig;
import pl.marcinm312.springquestionsanswers.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springquestionsanswers.config.security.SecurityMessagesConfig;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springquestionsanswers.config.security.utils.SessionUtils;
import pl.marcinm312.springquestionsanswers.question.model.QuestionEntity;
import pl.marcinm312.springquestionsanswers.question.model.dto.QuestionCreateUpdate;
import pl.marcinm312.springquestionsanswers.question.model.dto.QuestionGet;
import pl.marcinm312.springquestionsanswers.question.repository.QuestionRepository;
import pl.marcinm312.springquestionsanswers.question.service.QuestionManager;
import pl.marcinm312.springquestionsanswers.question.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springquestionsanswers.shared.file.ExcelGenerator;
import pl.marcinm312.springquestionsanswers.shared.file.PdfGenerator;
import pl.marcinm312.springquestionsanswers.shared.filter.Filter;
import pl.marcinm312.springquestionsanswers.mail.service.MailSender;
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
@WebMvcTest(controllers = QuestionWebController.class)
@ComponentScan(basePackageClasses = QuestionWebController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = QuestionWebController.class)
		})
@MockBeans({@MockBean(ActivationTokenRepo.class), @MockBean(MailChangeTokenRepo.class), @MockBean(MailSender.class),
		@MockBean(SessionUtils.class)})
@SpyBeans({@SpyBean(QuestionManager.class), @SpyBean(UserDetailsServiceImpl.class), @SpyBean(UserManager.class),
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class),
		@SpyBean(ExcelGenerator.class), @SpyBean(PdfGenerator.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class, PropertiesConfig.class})
@WebAppConfiguration
class QuestionWebControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private QuestionRepository questionRepository;

	@MockBean
	private UserRepo userRepo;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final UserEntity secondUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
	private final UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();

	@BeforeEach
	void setup() {

		given(questionRepository.getPaginatedQuestions(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"))))
				.willReturn(new PageImpl<>(QuestionDataProvider.prepareExampleQuestionsList()));
		given(questionRepository.getPaginatedQuestions(PageRequest.of(0, 5000, Sort.by(Sort.Direction.DESC, "id"))))
				.willReturn(new PageImpl<>(QuestionDataProvider.prepareExampleQuestionsList()));
		given(questionRepository.searchPaginatedQuestions("aaaa", PageRequest.of(0, 5,
				Sort.by(Sort.Direction.ASC, "id"))))
				.willReturn(new PageImpl<>(QuestionDataProvider.prepareExampleSearchedQuestionsList()));
		given(questionRepository.findById(1000L))
				.willReturn(Optional.of(QuestionDataProvider.prepareExampleQuestion()));
		given(questionRepository.findById(2000L)).willReturn(Optional.empty());

		given(userRepo.findByUsername("user")).willReturn(Optional.of(commonUser));
		given(userRepo.findByUsername("user2")).willReturn(Optional.of(secondUser));
		given(userRepo.findByUsername("admin")).willReturn(Optional.of(adminUser));

		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.webApplicationContext)
				.apply(springSecurity())
				.alwaysDo(print())
				.build();
	}

	@Test
	void questionsGet_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/questions/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@ParameterizedTest
	@MethodSource("examplesOfQuestionsGetUrls")
	void questionsGet_parameterized_success(String url, int arrayExpectedSize) throws Exception {

		ModelAndView modelAndView = mockMvc.perform(get(url).with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("questions"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(model().attributeExists("questionList", "userLogin"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		List<QuestionGet> questionsFromModel = (List<QuestionGet>) modelAndView.getModel().get("questionList");
		int arrayResultSize = questionsFromModel.size();
		Assertions.assertEquals(arrayExpectedSize, arrayResultSize);
	}

	private static Stream<Arguments> examplesOfQuestionsGetUrls() {

		return Stream.of(
				Arguments.of("/app/questions/", 3),
				Arguments.of("/app/questions/?keyword=aaaa&pageNo=-1&pageSize=0&sortField=TEXT&sortDirection=ASC", 1),
				Arguments.of("/app/questions/?keyword=aaaa&pageNo=1&pageSize=0&sortField=TEXT&sortDirection=ASC", 1),
				Arguments.of("/app/questions/?keyword=aaaa&pageNo=0&pageSize=5&sortField=TEXT&sortDirection=ASC", 1),
				Arguments.of("/app/questions/?keyword=aaaa&pageNo=1&pageSize=5&sortField=TEXT&sortDirection=ASC", 1),
				Arguments.of("/app/questions/?keyword=aaaa&pageNo=1&pageSize=5&sortField=ID&sortDirection=ASC", 1),
				Arguments.of("/app/questions/?pageNo=1&pageSize=5&sortField=TEXT&sortDirection=DESC", 3),
				Arguments.of("/app/questions/?pageNo=1&pageSize=5000&sortField=TEXT&sortDirection=DESC", 3)
		);
	}

	@Test
	void questionsGet_tooLargePageSize_badRequest() throws Exception {

		String url = "/app/questions/?pageNo=1&pageSize=5001&sortField=TEXT&sortDirection=DESC";
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
	void downloadFile_tooLargePageSize_badRequest(String url) throws Exception {

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
				Arguments.of("/app/questions/file-export/?fileType=PDF&pageNo=1&pageSize=5001&sortField=TEXT&sortDirection=DESC"),
				Arguments.of("/app/questions/file-export/?fileType=EXCEL&pageNo=1&pageSize=5001&sortField=TEXT&sortDirection=DESC")
		);
	}

	@Test
	void createQuestionView_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/questions/new/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@Test
	void createQuestionView_simpleCase_success() throws Exception {

		mockMvc.perform(
						get("/app/questions/new/")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("createQuestion"))
				.andExpect(model().attributeExists("question", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	void createQuestion_withAnonymousUser_redirectToLoginPage() throws Exception {

		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();
		mockMvc.perform(
						post("/app/questions/new/")
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void createQuestion_withoutCsrfToken_forbidden() throws Exception {

		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();
		mockMvc.perform(
						post("/app/questions/new/")
								.with(user("user").password("password"))
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isForbidden());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void createQuestion_withCsrfInvalidToken_forbidden() throws Exception {

		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();
		mockMvc.perform(
						post("/app/questions/new/")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isForbidden());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfGoodQuestions")
	void createQuestion_goodQuestion_success(QuestionCreateUpdate questionToRequest) throws Exception {

		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequest.getTitle(), questionToRequest.getDescription(), commonUser));
		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);

		mockMvc.perform(
						post("/app/questions/new/")
								.with(user("user").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(".."))
				.andExpect(view().name("redirect:.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(questionRepository, times(1)).save(any(QuestionEntity.class));
	}

	private static Stream<Arguments> examplesOfGoodQuestions() {

		return Stream.of(
			Arguments.of(QuestionDataProvider.prepareGoodQuestionToRequest()),
			Arguments.of(QuestionDataProvider.prepareGoodQuestionWithEmptyDescriptionToRequest()),
			Arguments.of(QuestionDataProvider.prepareGoodQuestionWithNullDescriptionToRequest())
		);
	}

	@ParameterizedTest
	@MethodSource("examplesOfCreateUpdateQuestionBadRequests")
	void createQuestion_incorrectQuestion_validationErrors(QuestionCreateUpdate questionToRequest, String expectedTitle)
			throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/new/")
								.with(user("user").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("createQuestion"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("question", "title"))
				.andExpect(model().attributeExists("question", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionCreateUpdate questionFromModel = (QuestionCreateUpdate) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedTitle, questionFromModel.getTitle());
		Assertions.assertEquals(questionToRequest.getDescription(), questionFromModel.getDescription());
		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	private static Stream<Arguments> examplesOfCreateUpdateQuestionBadRequests() {

		QuestionCreateUpdate questionWithTooShortTitle = QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest();
		QuestionCreateUpdate questionWithTooShortTitleAfterTrim = QuestionDataProvider.prepareQuestionWithTooShortTitleAfterTrimToRequest();
		QuestionCreateUpdate questionWithEmptyTitle = QuestionDataProvider.prepareQuestionWithEmptyTitleToRequest();
		return Stream.of(
			Arguments.of(questionWithTooShortTitle, questionWithTooShortTitle.getTitle()),
			Arguments.of(questionWithTooShortTitleAfterTrim, questionWithTooShortTitleAfterTrim.getTitle().trim()),
			Arguments.of(questionWithEmptyTitle, null)
		);
	}

	@Test
	void editQuestionView_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/questions/1000/edit/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@Test
	void editQuestionView_simpleCase_success() throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/1000/edit/")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("editQuestion"))
				.andExpect(model().attributeExists("question", "oldQuestion", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionGet oldQuestionFromModel = (QuestionGet) modelAndView.getModel().get("oldQuestion");
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		Assertions.assertEquals(expectedQuestion.getId(), oldQuestionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), oldQuestionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), oldQuestionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), oldQuestionFromModel.getUser());

		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());
	}

	@Test
	void editQuestionView_questionNotExists_notFoundMessage() throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/2000/edit/")
								.with(user("user").password("password")))
				.andExpect(status().isNotFound())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().attributeExists("message", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		String messageFromModel = (String) modelAndView.getModel().get("message");
		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
	}

	@Test
	void editQuestion_withAnonymousUser_redirectToLoginPage() throws Exception {

		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();
		mockMvc.perform(
						post("/app/questions/1000/edit/")
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void editQuestion_withoutCsrfToken_forbidden() throws Exception {

		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();
		mockMvc.perform(
						post("/app/questions/1000/edit/")
								.with(user("user").password("password"))
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isForbidden());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void editQuestion_withCsrfInvalidToken_forbidden() throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);

		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();
		mockMvc.perform(
						post("/app/questions/1000/edit/")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isForbidden());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfUpdateAnswerGoodRequests")
	void editQuestion_userUpdatesQuestion_success(QuestionCreateUpdate questionToRequest, UserEntity loggedUser) throws Exception {

		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequest.getTitle(), questionToRequest.getDescription(), loggedUser));
		given(userRepo.getUserFromAuthentication(any())).willReturn(loggedUser);

		String role = loggedUser.getRole().name().replace("ROLE_", "");
		mockMvc.perform(
						post("/app/questions/1000/edit/")
								.with(user(loggedUser.getUsername()).password("password").roles(role))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername(loggedUser.getUsername()).withRoles(role));

		verify(questionRepository, times(1)).save(any(QuestionEntity.class));
	}

	private static Stream<Arguments> examplesOfUpdateAnswerGoodRequests() {

		UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();
		UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		return Stream.of(
				Arguments.of(QuestionDataProvider.prepareGoodQuestionToRequest(), adminUser),
				Arguments.of(QuestionDataProvider.prepareGoodQuestionWithNullDescriptionToRequest(), adminUser),
				Arguments.of(QuestionDataProvider.prepareGoodQuestionWithEmptyDescriptionToRequest(), adminUser),
				Arguments.of(QuestionDataProvider.prepareGoodQuestionToRequest(), commonUser),
				Arguments.of(QuestionDataProvider.prepareGoodQuestionWithNullDescriptionToRequest(), commonUser),
				Arguments.of(QuestionDataProvider.prepareGoodQuestionWithEmptyDescriptionToRequest(), commonUser)
		);
	}

	@ParameterizedTest
	@MethodSource("examplesOfCreateUpdateQuestionBadRequests")
	void editQuestion_incorrectQuestion_validationErrors(QuestionCreateUpdate questionToRequest, String expectedTitle)
			throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/1000/edit/")
								.with(user("user").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("editQuestion"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeExists("userLogin", "question", "oldQuestion"))
				.andExpect(model().attributeHasFieldErrors("question", "title"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionCreateUpdate questionFromModel = (QuestionCreateUpdate) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedTitle, questionFromModel.getTitle());
		Assertions.assertEquals(questionToRequest.getDescription(), questionFromModel.getDescription());

		QuestionEntity expectedOldQuestion = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet oldQuestionFromModel = (QuestionGet) modelAndView.getModel().get("oldQuestion");
		Assertions.assertEquals(expectedOldQuestion.getId(), oldQuestionFromModel.getId());
		Assertions.assertEquals(expectedOldQuestion.getTitle(), oldQuestionFromModel.getTitle());
		Assertions.assertEquals(expectedOldQuestion.getDescription(), oldQuestionFromModel.getDescription());
		Assertions.assertEquals(expectedOldQuestion.getUser().getUsername(), oldQuestionFromModel.getUser());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void editQuestion_userUpdatesAnotherUsersQuestion_changeNotAllowed() throws Exception {

		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequest.getTitle(), questionToRequest.getDescription(), secondUser));
		given(userRepo.getUserFromAuthentication(any())).willReturn(secondUser);

		mockMvc.perform(
						post("/app/questions/1000/edit/")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isForbidden())
				.andExpect(view().name("changeNotAllowed"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"));

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void editQuestion_questionNotExists_notFoundMessage() throws Exception {

		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequest.getTitle(), questionToRequest.getDescription(), secondUser));
		given(userRepo.getUserFromAuthentication(any())).willReturn(secondUser);

	 	ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/2000/edit/")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isNotFound())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		String messageFromModel = (String) modelAndView.getModel().get("message");
		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void removeQuestionView_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/questions/1000/delete/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@Test
	void removeQuestionView_simpleCase_success() throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/1000/delete/")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("deleteQuestion"))
				.andExpect(model().attributeExists("question", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionGet questionFromModel2 = (QuestionGet) modelAndView.getModel().get("question");
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel2.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel2.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel2.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel2.getUser());
	}

	@Test
	void removeQuestionView_questionNotExists_notFoundMessage() throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/2000/delete/")
								.with(user("user").password("password")))
				.andExpect(status().isNotFound())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().attributeExists("message", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		String messageFromModel = (String) modelAndView.getModel().get("message");
		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
	}

	@Test
	void removeQuestion_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						post("/app/questions/1000/delete/")
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());

		verify(questionRepository, never()).delete(any(QuestionEntity.class));
	}

	@Test
	void removeQuestion_withoutCsrfToken_forbidden() throws Exception {

		mockMvc.perform(
						post("/app/questions/1000/delete/")
								.with(user("user").password("password")))
				.andExpect(status().isForbidden());

		verify(questionRepository, never()).delete(any(QuestionEntity.class));
	}

	@Test
	void removeQuestion_withCsrfInvalidToken_forbidden() throws Exception {

		mockMvc.perform(
						post("/app/questions/1000/delete/")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken()))
				.andExpect(status().isForbidden());

		verify(questionRepository, never()).delete(any(QuestionEntity.class));
	}

	@ParameterizedTest
	@MethodSource("examplesOfSuccessfullyDeleteQuestion")
	void removeQuestion_userDeletesQuestion_success(UserEntity loggedUser) throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(loggedUser);

		String role = loggedUser.getRole().name().replace("ROLE_", "");
		mockMvc.perform(
						post("/app/questions/1000/delete/")
								.with(user(loggedUser.getUsername()).password("password").roles(role))
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername(loggedUser.getUsername()).withRoles(role));

		verify(questionRepository, times(1)).delete(any(QuestionEntity.class));
	}

	private static Stream<Arguments> examplesOfSuccessfullyDeleteQuestion() {

		UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();
		UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		return Stream.of(
				Arguments.of(commonUser),
				Arguments.of(adminUser)
		);
	}

	@Test
	void removeQuestion_questionNotExists_notFoundMessage() throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(secondUser);

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/2000/delete/")
								.with(user("user2").password("password"))
								.with(csrf()))
				.andExpect(status().isNotFound())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		String messageFromModel = (String) modelAndView.getModel().get("message");
		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
		verify(questionRepository, never()).delete(any(QuestionEntity.class));
	}

	@Test
	void removeQuestion_userDeletesAnotherUsersQuestion_changeNotAllowed() throws Exception {

		given(userRepo.getUserFromAuthentication(any())).willReturn(secondUser);

		mockMvc.perform(
						post("/app/questions/1000/delete/")
								.with(user("user2").password("password"))
								.with(csrf()))
				.andExpect(status().isForbidden())
				.andExpect(view().name("changeNotAllowed"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"));

		verify(questionRepository, never()).delete(any(QuestionEntity.class));
	}

	@Test
	void downloadPdf_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/questions/file-export/?fileType=PDF"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@ParameterizedTest
	@MethodSource("examplesOfDownloadPdfUrls")
	void downloadPdf_parameterized_success(String url) throws Exception {

		mockMvc.perform(get(url).with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	private static Stream<Arguments> examplesOfDownloadPdfUrls() {

		return Stream.of(
				Arguments.of("/app/questions/file-export/?fileType=PDF"),
				Arguments.of("/app/questions/file-export/?fileType=PDF&keyword=aaaa&pageNo=-1&pageSize=0&sortField=TEXT&sortDirection=ASC"),
				Arguments.of("/app/questions/file-export/?fileType=PDF&keyword=aaaa&pageNo=1&pageSize=0&sortField=TEXT&sortDirection=ASC"),
				Arguments.of("/app/questions/file-export/?fileType=PDF&keyword=aaaa&pageNo=0&pageSize=5&sortField=TEXT&sortDirection=ASC"),
				Arguments.of("/app/questions/file-export/?fileType=PDF&keyword=aaaa&pageNo=1&pageSize=5&sortField=TEXT&sortDirection=ASC"),
				Arguments.of("/app/questions/file-export/?fileType=PDF&keyword=aaaa&pageNo=1&pageSize=5&sortField=ID&sortDirection=ASC"),
				Arguments.of("/app/questions/file-export/?fileType=PDF&pageNo=1&pageSize=5&sortField=TEXT&sortDirection=DESC"),
				Arguments.of("/app/questions/file-export/?fileType=PDF&pageNo=1&pageSize=5000&sortField=TEXT&sortDirection=DESC")
		);
	}

	@Test
	void downloadExcel_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/questions/file-export/?fileType=EXCEL"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage/"))
				.andExpect(unauthenticated());
	}

	@ParameterizedTest
	@MethodSource("examplesOfDownloadExcelUrls")
	void downloadExcel_parameterized_success(String url) throws Exception {

		mockMvc.perform(get(url).with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	private static Stream<Arguments> examplesOfDownloadExcelUrls() {

		return Stream.of(
				Arguments.of("/app/questions/file-export/?fileType=EXCEL"),
				Arguments.of("/app/questions/file-export/?fileType=EXCEL&keyword=aaaa&pageNo=-1&pageSize=0&sortField=TEXT&sortDirection=ASC"),
				Arguments.of("/app/questions/file-export/?fileType=EXCEL&keyword=aaaa&pageNo=1&pageSize=0&sortField=TEXT&sortDirection=ASC"),
				Arguments.of("/app/questions/file-export/?fileType=EXCEL&keyword=aaaa&pageNo=0&pageSize=5&sortField=TEXT&sortDirection=ASC"),
				Arguments.of("/app/questions/file-export/?fileType=EXCEL&keyword=aaaa&pageNo=1&pageSize=5&sortField=TEXT&sortDirection=ASC"),
				Arguments.of("/app/questions/file-export/?fileType=EXCEL&keyword=aaaa&pageNo=1&pageSize=5&sortField=ID&sortDirection=ASC"),
				Arguments.of("/app/questions/file-export/?fileType=EXCEL&pageNo=1&pageSize=5&sortField=TEXT&sortDirection=DESC"),
				Arguments.of("/app/questions/file-export/?fileType=EXCEL&pageNo=1&pageSize=5000&sortField=TEXT&sortDirection=DESC")
		);
	}
}
