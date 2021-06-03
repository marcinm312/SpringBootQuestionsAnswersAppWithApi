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
import org.springframework.security.core.Authentication;
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
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
@MockBeans({@MockBean(UserRepo.class)})
@SpyBeans({@SpyBean(QuestionManager.class), @SpyBean(ExcelGenerator.class), @SpyBean(PdfGenerator.class), @SpyBean(UserDetailsServiceImpl.class)})
@Import({MultiHttpSecurityCustomConfig.class})
@WebAppConfiguration
class QuestionWebControllerTest {

	private MockMvc mockMvc;

	@MockBean
	QuestionRepository questionRepository;

	@MockBean
	UserManager userManager;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	void setup() {
		given(questionRepository.findAll()).willReturn(QuestionDataProvider.prepareExampleQuestionsList());

		User user = UserDataProvider.prepareExampleGoodUser();
		given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(user);

		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.webApplicationContext)
				.apply(springSecurity())
				.alwaysDo(print())
				.build();
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
	void createQuestion_witCsrfInvalidToken_forbidden() throws Exception {
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
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(model().attributeExists("question"))
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
				.andExpect(model().attribute("userLogin", "user"))
				.andExpect(model().attributeExists("question"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		QuestionCreateUpdate questionFromModel = (QuestionCreateUpdate) modelAndView.getModel().get("question");
		Assertions.assertEquals(questionToRequest.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(questionToRequest.getDescription(), questionFromModel.getDescription());
	}

	@Test
	@WithAnonymousUser
	void downloadPdf_withAnonymousUser_redirectToLoginPage() throws Exception {
		mockMvc.perform(
				get("/app/questions/pdf-export")
						.with(csrf()))
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
				get("/app/questions/excel-export")
						.with(csrf()))
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