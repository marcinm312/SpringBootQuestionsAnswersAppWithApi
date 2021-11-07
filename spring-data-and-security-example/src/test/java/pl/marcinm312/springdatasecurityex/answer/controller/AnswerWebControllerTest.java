package pl.marcinm312.springdatasecurityex.answer.controller;

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
import pl.marcinm312.springdatasecurityex.answer.model.AnswerEntity;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerCreateUpdate;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.answer.repository.AnswerRepository;
import pl.marcinm312.springdatasecurityex.answer.service.AnswerManager;
import pl.marcinm312.springdatasecurityex.answer.testdataprovider.AnswerDataProvider;
import pl.marcinm312.springdatasecurityex.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.config.security.SecurityMessagesConfig;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springdatasecurityex.config.security.utils.SessionUtils;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.question.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.question.service.QuestionManager;
import pl.marcinm312.springdatasecurityex.question.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.shared.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.shared.file.PdfGenerator;
import pl.marcinm312.springdatasecurityex.shared.mail.MailService;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.user.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;

import javax.mail.MessagingException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
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
@MockBeans({@MockBean(TokenRepo.class), @MockBean(SessionUtils.class)})
@SpyBeans({@SpyBean(QuestionManager.class), @SpyBean(AnswerManager.class), @SpyBean(UserDetailsServiceImpl.class),
		@SpyBean(UserManager.class), @SpyBean(ExcelGenerator.class), @SpyBean(PdfGenerator.class),
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class)})
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

	private final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final UserEntity secondUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
	private final UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();

	private final QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();

	@BeforeEach
	void setup() throws MessagingException {
		AnswerEntity answer = AnswerDataProvider.prepareExampleAnswer();

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
		doNothing().when(answerRepository).delete(isA(AnswerEntity.class));

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
	void answersGet_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/answers"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void answersGet_simpleCase_success() throws Exception {
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/1000/answers")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("answers"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		List<AnswerGet> answersFromModel = (List<AnswerGet>) modelAndView.getModel().get("answerList");
		int arrayExpectedSize = 3;
		int arrayResultSize = answersFromModel.size();
		Assertions.assertEquals(arrayExpectedSize, arrayResultSize);

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
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		String messageFromModel = (String) modelAndView.getModel().get("message");
		String expectedMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedMessage, messageFromModel);
	}

	@Test
	@WithAnonymousUser
	void createAnswer_withAnonymousUser_redirectToLoginPage() throws Exception {
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();

		mockMvc.perform(
						post("/app/questions/1000/answers/new")
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void createAnswer_withoutCsrfToken_forbidden() throws Exception {
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();

		mockMvc.perform(
						post("/app/questions/1000/answers/new")
								.with(user("user2").password("password"))
								.param("text", answerToRequest.getText()))
				.andExpect(status().isForbidden());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void createAnswer_withCsrfInvalidToken_forbidden() throws Exception {
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();

		mockMvc.perform(
						post("/app/questions/1000/answers/new")
								.with(user("user2").password("password"))
								.with(csrf().useInvalidToken())
								.param("text", answerToRequest.getText()))
				.andExpect(status().isForbidden());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void createAnswer_simpleCase_success() throws Exception {
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(answerRepository.save(any(AnswerEntity.class))).willReturn(new AnswerEntity(answerToRequest.getText(), user));

		mockMvc.perform(
						post("/app/questions/1000/answers/new")
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
	@WithMockUser(username = "user2")
	void createAnswer_tooShortText_validationErrors() throws Exception {
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareAnswerWithTooShortTextToRequest();

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/1000/answers/new")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(view().name("createAnswer"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("answer", "text"))
				.andExpect(model().attributeExists("question", "userLogin", "answer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());

		AnswerCreateUpdate answerFromModel = (AnswerCreateUpdate) modelAndView.getModel().get("answer");
		Assertions.assertEquals(answerToRequest.getText(), answerFromModel.getText());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void createAnswer_tooShortTextAfterTrim_validationErrors() throws Exception {
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareAnswerWithTooShortTextAfterTrimToRequest();

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/1000/answers/new")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(view().name("createAnswer"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("answer", "text"))
				.andExpect(model().attributeExists("question", "userLogin", "answer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());

		AnswerCreateUpdate answerFromModel = (AnswerCreateUpdate) modelAndView.getModel().get("answer");
		Assertions.assertEquals(answerToRequest.getText().trim(), answerFromModel.getText());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void createAnswer_emptyText_validationErrors() throws Exception {
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareAnswerWithEmptyTextToRequest();

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/1000/answers/new")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(view().name("createAnswer"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("answer", "text"))
				.andExpect(model().attributeExists("question", "userLogin", "answer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());

		AnswerCreateUpdate answerFromModel = (AnswerCreateUpdate) modelAndView.getModel().get("answer");
		Assertions.assertNull(answerFromModel.getText());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	@WithAnonymousUser
	void createAnswerView_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/answers/new"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user2")
	void createAnswerView_simpleCase_success() throws Exception {
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/1000/answers/new")
								.with(user("user2").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("createAnswer"))
				.andExpect(model().attributeExists("question", "userLogin", "answer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());
	}

	@Test
	@WithMockUser(username = "user2")
	void createAnswerView_questionNotExists_notFoundMessage() throws Exception {
		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/2000/answers/new")
								.with(user("user2").password("password")))
				.andExpect(status().isOk())
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
	@WithAnonymousUser
	void editAnswer_withAnonymousUser_redirectToLoginPage() throws Exception {
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();

		mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit")
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void editAnswer_withoutCsrfToken_forbidden() throws Exception {
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();

		mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit")
								.with(user("user2").password("password"))
								.param("text", answerToRequest.getText()))
				.andExpect(status().isForbidden());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void editAnswer_withCsrfInvalidToken_forbidden() throws Exception {
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();

		mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit")
								.with(user("user2").password("password"))
								.with(csrf().useInvalidToken())
								.param("text", answerToRequest.getText()))
				.andExpect(status().isForbidden());

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void editAnswer_userUpdatesHisOwnAnswer_success() throws Exception {
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(answerRepository.save(any(AnswerEntity.class))).willReturn(new AnswerEntity(answerToRequest.getText(), user));

		mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("user2").withRoles("USER"));

		verify(mailService, times(1)).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, times(1)).save(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void editAnswer_tooShortText_validationErrors() throws Exception {
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareAnswerWithTooShortTextToRequest();
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(answerRepository.save(any(AnswerEntity.class))).willReturn(new AnswerEntity(answerToRequest.getText(), user));

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(view().name("editAnswer"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("answer", "text"))
				.andExpect(model().attributeExists("question", "userLogin", "answer", "oldAnswer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());

		AnswerCreateUpdate answerFromModel = (AnswerCreateUpdate) modelAndView.getModel().get("answer");
		Assertions.assertEquals(answerToRequest.getText(), answerFromModel.getText());

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
	@WithMockUser(username = "user2")
	void editAnswer_emptyText_validationErrors() throws Exception {
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareAnswerWithEmptyTextToRequest();
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(answerRepository.save(any(AnswerEntity.class))).willReturn(new AnswerEntity(answerToRequest.getText(), user));

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(view().name("editAnswer"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("answer", "text"))
				.andExpect(model().attributeExists("question", "userLogin", "answer", "oldAnswer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());

		AnswerCreateUpdate answerFromModel = (AnswerCreateUpdate) modelAndView.getModel().get("answer");
		Assertions.assertNull(answerFromModel.getText());

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
	@WithMockUser(username = "administrator", roles = {"ADMIN"})
	void editAnswer_administratorUpdatesAnotherUsersAnswer_success() throws Exception {
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(answerRepository.save(any(AnswerEntity.class))).willReturn(new AnswerEntity(answerToRequest.getText(), user));

		mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit")
								.with(user("administrator").password("password").roles("ADMIN"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("administrator").withRoles("ADMIN"));

		verify(mailService, times(1)).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, times(1)).save(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user")
	void editAnswer_userUpdatesAnotherUsersAnswer_changeNotAllowed() throws Exception {
		AnswerCreateUpdate answerToRequest = AnswerDataProvider.prepareGoodAnswerToRequest();

		mockMvc.perform(
						post("/app/questions/1000/answers/1000/edit")
								.with(user("user").password("password"))
								.with(csrf())
								.param("text", answerToRequest.getText()))
				.andExpect(status().isOk())
				.andExpect(view().name("changeNotAllowed"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(mailService, never()).sendMail(eq(question.getUser().getEmail()),
				any(String.class), any(String.class), eq(true));
		verify(answerRepository, never()).save(any(AnswerEntity.class));
	}

	@Test
	@WithAnonymousUser
	void editAnswerView_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/answers/1000/edit"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user2")
	void editAnswerView_simpleCase_success() throws Exception {
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		AnswerEntity expectedAnswer = AnswerDataProvider.prepareExampleAnswer();

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/1000/answers/1000/edit")
								.with(user("user2").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("editAnswer"))
				.andExpect(model().attributeExists("question", "userLogin", "answer", "oldAnswer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());

		AnswerGet answerFromModel = (AnswerGet) modelAndView.getModel().get("answer");
		Assertions.assertEquals(expectedAnswer.getId(), answerFromModel.getId());
		Assertions.assertEquals(expectedAnswer.getText(), answerFromModel.getText());
		Assertions.assertEquals(expectedAnswer.getUser().getUsername(), answerFromModel.getUser());

		AnswerGet oldAnswerFromModel = (AnswerGet) modelAndView.getModel().get("oldAnswer");
		Assertions.assertEquals(expectedAnswer.getId(), oldAnswerFromModel.getId());
		Assertions.assertEquals(expectedAnswer.getText(), oldAnswerFromModel.getText());
		Assertions.assertEquals(expectedAnswer.getUser().getUsername(), oldAnswerFromModel.getUser());
	}

	@WithMockUser(username = "user2")
	@ParameterizedTest(name = "{index} ''{2}''")
	@MethodSource("examplesOfEditNotFoundUrlsAndErrorMessages")
	void editAnswerView_questionOrAnswerNotExists_notFoundMessage(String url, String expectedErrorMessage,
																	String nameOfTestCase) throws Exception {
		ModelAndView modelAndView = mockMvc.perform(
						get(url)
								.with(user("user2").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().attributeExists("message", "userLogin"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		String messageFromModel = (String) modelAndView.getModel().get("message");
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
	}

	private static Stream<Arguments> examplesOfEditNotFoundUrlsAndErrorMessages() {
		return Stream.of(
				Arguments.of("/app/questions/2000/answers/1000/edit", "Nie znaleziono odpowiedzi o id: 1000 na pytanie o id: 2000",
						"questionNotExists_notFound"),
				Arguments.of("/app/questions/1000/answers/2000/edit", "Nie znaleziono odpowiedzi o id: 2000 na pytanie o id: 1000",
						"answerNotExists_notFound"),
				Arguments.of("/app/questions/2000/answers/2000/edit", "Nie znaleziono odpowiedzi o id: 2000 na pytanie o id: 2000",
						"answerAndQuestionNotExists_notFound")
		);
	}

	@Test
	@WithAnonymousUser
	void removeAnswer_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						post("/app/questions/1000/answers/1000/delete")
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());

		verify(answerRepository, never()).delete(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void removeAnswer_withoutCsrfToken_forbidden() throws Exception {
		mockMvc.perform(
						post("/app/questions/1000/answers/1000/delete")
								.with(user("user").password("password")))
				.andExpect(status().isForbidden());

		verify(answerRepository, never()).delete(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void removeAnswer_withCsrfInvalidToken_forbidden() throws Exception {
		mockMvc.perform(
						post("/app/questions/1000/answers/1000/delete")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken()))
				.andExpect(status().isForbidden());

		verify(answerRepository, never()).delete(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void removeAnswer_userDeletesHisOwnAnswer_success() throws Exception {
		mockMvc.perform(
			post("/app/questions/1000/answers/1000/delete")
					.with(user("user2").password("password"))
					.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("user2").withRoles("USER"));

		verify(answerRepository, times(1)).delete(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "administrator", roles = {"ADMIN"})
	void removeAnswer_administratorDeletesAnotherUsersAnswer_success() throws Exception {
		mockMvc.perform(
						post("/app/questions/1000/answers/1000/delete")
								.with(user("administrator").password("password").roles("ADMIN"))
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("administrator").withRoles("ADMIN"));

		verify(answerRepository, times(1)).delete(any(AnswerEntity.class));
	}

	@Test
	@WithMockUser(username = "user")
	void removeAnswer_userDeletesAnotherUsersAnswer_changeNotAllowed() throws Exception {
		mockMvc.perform(
						post("/app/questions/1000/answers/1000/delete")
								.with(user("user").password("password"))
								.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("changeNotAllowed"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(answerRepository, never()).delete(any(AnswerEntity.class));
	}

	@Test
	@WithAnonymousUser
	void removeAnswerView_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/answers/1000/delete"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user2")
	void removeAnswerView_simpleCase_success() throws Exception {
		QuestionEntity expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		AnswerEntity expectedAnswer = AnswerDataProvider.prepareExampleAnswer();

		ModelAndView modelAndView = mockMvc.perform(
				get("/app/questions/1000/answers/1000/delete")
						.with(user("user2").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("deleteAnswer"))
				.andExpect(model().attributeExists("question", "userLogin", "answer"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());

		AnswerGet answerFromModel = (AnswerGet) modelAndView.getModel().get("answer");
		Assertions.assertEquals(expectedAnswer.getId(), answerFromModel.getId());
		Assertions.assertEquals(expectedAnswer.getText(), answerFromModel.getText());
		Assertions.assertEquals(expectedAnswer.getUser().getUsername(), answerFromModel.getUser());
	}

	@WithMockUser(username = "user2")
	@ParameterizedTest(name = "{index} ''{2}''")
	@MethodSource("examplesOfDeleteNotFoundUrlsAndErrorMessages")
	void removeAnswerView_questionOrAnswerNotExists_notFoundMessage(String url, String expectedErrorMessage,
																	String nameOfTestCase) throws Exception {
		ModelAndView modelAndView = mockMvc.perform(
						get(url)
								.with(user("user2").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().attributeExists("message", "userLogin"))
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		String messageFromModel = (String) modelAndView.getModel().get("message");
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
	}

	private static Stream<Arguments> examplesOfDeleteNotFoundUrlsAndErrorMessages() {
		return Stream.of(
				Arguments.of("/app/questions/2000/answers/1000/delete", "Nie znaleziono odpowiedzi o id: 1000 na pytanie o id: 2000",
						"questionNotExists_notFound"),
				Arguments.of("/app/questions/1000/answers/2000/delete", "Nie znaleziono odpowiedzi o id: 2000 na pytanie o id: 1000",
						"answerNotExists_notFound"),
				Arguments.of("/app/questions/2000/answers/2000/delete", "Nie znaleziono odpowiedzi o id: 2000 na pytanie o id: 2000",
						"answerAndQuestionNotExists_notFound")
		);
	}

	@Test
	@WithAnonymousUser
	void downloadPdf_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/answers/pdf-export"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void downloadPdf_simpleCase_success() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/answers/pdf-export")
								.with(user("user").password("password")))
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
				.andExpect(redirectedUrl("http://localhost/loginPage"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void downloadExcel_simpleCase_success() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/answers/excel-export")
								.with(user("user").password("password")))
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
						get(url).with(user("user").password("password")))
				.andExpect(status().isNotFound())
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
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