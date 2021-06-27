package pl.marcinm312.springdatasecurityex.controller.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import pl.marcinm312.springdatasecurityex.config.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.model.question.Question;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.MailService;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.utils.SessionUtils;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
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
@MockBeans({@MockBean(TokenRepo.class), @MockBean(MailService.class), @MockBean(SessionUtils.class)})
@SpyBeans({@SpyBean(QuestionManager.class), @SpyBean(ExcelGenerator.class), @SpyBean(PdfGenerator.class),
		@SpyBean(UserDetailsServiceImpl.class), @SpyBean(UserManager.class)})
@Import({MultiHttpSecurityCustomConfig.class})
@WebAppConfiguration
class QuestionWebControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private QuestionRepository questionRepository;

	@MockBean
	private UserRepo userRepo;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final User commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final User secondUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
	private final User adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();

	@BeforeEach
	void setup() {
		Question question = QuestionDataProvider.prepareExampleQuestion();
		given(questionRepository.findAllByOrderByIdDesc())
				.willReturn(QuestionDataProvider.prepareExampleQuestionsList());
		given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
		given(questionRepository.findById(2000L)).willReturn(Optional.empty());
		doNothing().when(questionRepository).delete(isA(Question.class));

		given(userRepo.findByUsername("user")).willReturn(Optional.of(commonUser));
		given(userRepo.findByUsername("user2")).willReturn(Optional.of(secondUser));
		given(userRepo.findByUsername("administrator")).willReturn(Optional.of(adminUser));

		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.webApplicationContext)
				.apply(springSecurity())
				.alwaysDo(print())
				.build();
	}

	@Test
	@WithAnonymousUser
	void questionsGet_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void questionsGet_simpleCase_success() throws Exception {
		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("questions"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(model().attributeExists("questionList", "userLogin"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		List<QuestionGet> questionsFromModel = (List<QuestionGet>) modelAndView.getModel().get("questionList");
		int arrayExpectedSize = 3;
		int arrayResultSize = questionsFromModel.size();
		Assertions.assertEquals(arrayExpectedSize, arrayResultSize);
	}

	@Test
	@WithAnonymousUser
	void createQuestionView_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions/new"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestionView_simpleCase_success() throws Exception {
		mockMvc.perform(
						get("/app/questions/new")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("createQuestion"))
				.andExpect(model().attributeExists("question", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithAnonymousUser
	void createQuestion_withAnonymousUser_redirectToLoginPage() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();

		mockMvc.perform(
						post("/app/questions/new")
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_withoutCsrfToken_forbidden() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();

		mockMvc.perform(
						post("/app/questions/new")
								.with(user("user").password("password"))
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_withCsrfInvalidToken_forbidden() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();

		mockMvc.perform(
						post("/app/questions/new")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_simpleCase_success() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(Question.class)))
				.willReturn(new Question(questionToRequest.getTitle(), questionToRequest.getDescription()));

		mockMvc.perform(
						post("/app/questions/new")
								.with(user("user").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(".."))
				.andExpect(view().name("redirect:.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_emptyDescription_success() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionWithEmptyDescriptionToRequest();
		given(questionRepository.save(any(Question.class)))
				.willReturn(new Question(questionToRequest.getTitle(), questionToRequest.getDescription()));

		mockMvc.perform(
						post("/app/questions/new")
								.with(user("user").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(".."))
				.andExpect(view().name("redirect:.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_tooShortTitle_validationErrors() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest();

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/new")
								.with(user("user").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(view().name("createQuestion"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("question", "title"))
				.andExpect(model().attributeExists("question", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionCreateUpdate questionFromModel = (QuestionCreateUpdate) modelAndView.getModel().get("question");
		Assertions.assertEquals(questionToRequest.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(questionToRequest.getDescription(), questionFromModel.getDescription());
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_emptyTitle_validationErrors() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareQuestionWithEmptyTitleToRequest();

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/new")
								.with(user("user").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(view().name("createQuestion"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("question", "title"))
				.andExpect(model().attributeExists("question", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionCreateUpdate questionFromModel = (QuestionCreateUpdate) modelAndView.getModel().get("question");
		Assertions.assertEquals(questionToRequest.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(questionToRequest.getDescription(), questionFromModel.getDescription());
	}

	@Test
	@WithAnonymousUser
	void editQuestionView_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/edit"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void editQuestionView_simpleCase_success() throws Exception {
		Question expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/1000/edit")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("editQuestion"))
				.andExpect(model().attributeExists("question", "oldQuestion", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		QuestionGet questionFromModel = (QuestionGet) modelAndView.getModel().get("oldQuestion");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel.getUser());

		QuestionGet questionFromModel2 = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel2.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel2.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel2.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel2.getUser());
	}

	@Test
	@WithMockUser(username = "user")
	void editQuestionView_questionNotExists_notFoundMessage() throws Exception {
		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/2000/edit")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().attributeExists("message", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		String messageFromModel = (String) modelAndView.getModel().get("message");
		String expectedErrorMessage = "Question not found with id 2000";
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
	}

	@Test
	@WithAnonymousUser
	void editQuestion_withAnonymousUser_redirectToLoginPage() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();

		mockMvc.perform(
						post("/app/questions/1000/edit")
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void editQuestion_withoutCsrfToken_forbidden() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();

		mockMvc.perform(
						post("/app/questions/1000/edit")
								.with(user("user").password("password"))
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "user")
	void editQuestion_witCsrfInvalidToken_forbidden() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();

		mockMvc.perform(
						post("/app/questions/1000/edit")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "user")
	void editQuestion_userUpdatesHisOwnQuestion_success() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(Question.class)))
				.willReturn(new Question(questionToRequest.getTitle(), questionToRequest.getDescription()));

		mockMvc.perform(
						post("/app/questions/1000/edit")
								.with(user("user").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithMockUser(username = "user")
	void editQuestion_emptyDescription_success() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionWithNullDescriptionToRequest();
		given(questionRepository.save(any(Question.class)))
				.willReturn(new Question(questionToRequest.getTitle(), questionToRequest.getDescription()));

		mockMvc.perform(
						post("/app/questions/1000/edit")
								.with(user("user").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithMockUser(username = "user")
	void editQuestion_tooShortTitle_validationErrors() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest();

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/1000/edit")
								.with(user("user").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(view().name("editQuestion"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeExists("userLogin", "question", "oldQuestion"))
				.andExpect(model().attributeHasFieldErrors("question", "title"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionCreateUpdate questionFromModel = (QuestionCreateUpdate) modelAndView.getModel().get("question");
		Assertions.assertEquals(questionToRequest.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(questionToRequest.getDescription(), questionFromModel.getDescription());

		Question expectedOldQuestion = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet oldQuestionFromModel = (QuestionGet) modelAndView.getModel().get("oldQuestion");
		Assertions.assertEquals(expectedOldQuestion.getId(), oldQuestionFromModel.getId());
		Assertions.assertEquals(expectedOldQuestion.getTitle(), oldQuestionFromModel.getTitle());
		Assertions.assertEquals(expectedOldQuestion.getDescription(), oldQuestionFromModel.getDescription());
		Assertions.assertEquals(expectedOldQuestion.getUser().getUsername(), oldQuestionFromModel.getUser());
	}

	@Test
	@WithMockUser(username = "user")
	void editQuestion_emptyTitle_validationErrors() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareQuestionWithEmptyTitleToRequest();

		ModelAndView modelAndView = mockMvc.perform(
						post("/app/questions/1000/edit")
								.with(user("user").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(view().name("editQuestion"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeExists("userLogin", "question", "oldQuestion"))
				.andExpect(model().attributeHasFieldErrors("question", "title"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionCreateUpdate questionFromModel = (QuestionCreateUpdate) modelAndView.getModel().get("question");
		Assertions.assertEquals(questionToRequest.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(questionToRequest.getDescription(), questionFromModel.getDescription());

		Question expectedOldQuestion = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet oldQuestionFromModel = (QuestionGet) modelAndView.getModel().get("oldQuestion");
		Assertions.assertEquals(expectedOldQuestion.getId(), oldQuestionFromModel.getId());
		Assertions.assertEquals(expectedOldQuestion.getTitle(), oldQuestionFromModel.getTitle());
		Assertions.assertEquals(expectedOldQuestion.getDescription(), oldQuestionFromModel.getDescription());
		Assertions.assertEquals(expectedOldQuestion.getUser().getUsername(), oldQuestionFromModel.getUser());
	}

	@Test
	@WithMockUser(username = "administrator", roles = {"ADMIN"})
	void editQuestion_administratorUpdatesAnotherUsersQuestion_success() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(Question.class)))
				.willReturn(new Question(questionToRequest.getTitle(), questionToRequest.getDescription()));

		mockMvc.perform(
						post("/app/questions/1000/edit")
								.with(user("administrator").password("password").roles("ADMIN"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("administrator").withRoles("ADMIN"));
	}

	@Test
	@WithMockUser(username = "user2")
	void editQuestion_userUpdatesAnotherUsersQuestion_changeNotAllowed() throws Exception {
		QuestionCreateUpdate questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(Question.class)))
				.willReturn(new Question(questionToRequest.getTitle(), questionToRequest.getDescription()));

		mockMvc.perform(
						post("/app/questions/1000/edit")
								.with(user("user2").password("password"))
								.with(csrf())
								.param("title", questionToRequest.getTitle())
								.param("description", questionToRequest.getDescription()))
				.andExpect(status().isOk())
				.andExpect(view().name("changeNotAllowed"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"));
	}

	@Test
	@WithAnonymousUser
	void removeQuestionView_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions/1000/delete"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void removeQuestionView_simpleCase_success() throws Exception {
		Question expectedQuestion = QuestionDataProvider.prepareExampleQuestion();
		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/1000/delete")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("deleteQuestion"))
				.andExpect(model().attributeExists("question", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		QuestionGet questionFromModel2 = (QuestionGet) modelAndView.getModel().get("question");
		Assertions.assertEquals(expectedQuestion.getId(), questionFromModel2.getId());
		Assertions.assertEquals(expectedQuestion.getTitle(), questionFromModel2.getTitle());
		Assertions.assertEquals(expectedQuestion.getDescription(), questionFromModel2.getDescription());
		Assertions.assertEquals(expectedQuestion.getUser().getUsername(), questionFromModel2.getUser());
	}

	@Test
	@WithMockUser(username = "user")
	void removeQuestionView_questionNotExists_notFoundMessage() throws Exception {
		ModelAndView modelAndView = mockMvc.perform(
						get("/app/questions/2000/delete")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(view().name("resourceNotFound"))
				.andExpect(model().attributeExists("message", "userLogin"))
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;

		String messageFromModel = (String) modelAndView.getModel().get("message");
		String expectedErrorMessage = "Question not found with id 2000";
		Assertions.assertEquals(expectedErrorMessage, messageFromModel);
	}

	@Test
	@WithAnonymousUser
	void removeQuestion_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						post("/app/questions/1000/delete")
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void removeQuestion_withoutCsrfToken_forbidden() throws Exception {
		mockMvc.perform(
						post("/app/questions/1000/delete")
								.with(user("user").password("password")))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "user")
	void removeQuestion_withCsrfInvalidToken_forbidden() throws Exception {
		mockMvc.perform(
						post("/app/questions/1000/delete")
								.with(user("user").password("password"))
								.with(csrf().useInvalidToken()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "user")
	void removeQuestion_userDeletesHisOwnQuestion_success() throws Exception {
		mockMvc.perform(
						post("/app/questions/1000/delete")
								.with(user("user").password("password"))
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithMockUser(username = "administrator", roles = {"ADMIN"})
	void removeQuestion_administratorDeletesAnotherUsersQuestion_success() throws Exception {
		mockMvc.perform(
						post("/app/questions/1000/delete")
								.with(user("administrator").password("password").roles("ADMIN"))
								.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("../.."))
				.andExpect(view().name("redirect:../.."))
				.andExpect(model().hasNoErrors())
				.andExpect(authenticated().withUsername("administrator").withRoles("ADMIN"));
	}

	@Test
	@WithMockUser(username = "user2")
	void removeQuestion_userDeletesAnotherUsersQuestion_changeNotAllowed() throws Exception {
		mockMvc.perform(
						post("/app/questions/1000/delete")
								.with(user("user2").password("password"))
								.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("changeNotAllowed"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("userLogin", "user2"))
				.andExpect(authenticated().withUsername("user2").withRoles("USER"));
	}

	@Test
	@WithAnonymousUser
	void downloadPdf_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
						get("/app/questions/pdf-export"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void downloadPdf_simpleCase_success() throws Exception {

		mockMvc.perform(
						get("/app/questions/pdf-export")
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
						get("/app/questions/excel-export"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void downloadExcel_simpleCase_success() throws Exception {
		mockMvc.perform(
						get("/app/questions/excel-export")
								.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}
}