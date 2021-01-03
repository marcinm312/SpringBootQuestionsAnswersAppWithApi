package pl.marcinm312.springdatasecurityex.controller.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.mock.mockito.SpyBeans;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import pl.marcinm312.springdatasecurityex.config.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
@ContextConfiguration(classes = {QuestionWebController.class})
@SpyBeans({@SpyBean(QuestionManager.class), @SpyBean(ExcelGenerator.class), @SpyBean(PdfGenerator.class)})
@Import({MultiHttpSecurityCustomConfig.FormLoginWebSecurityConfigurationAdapter.class})
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
		Question questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();

		String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";

		HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
		CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

		mockMvc.perform(
				post("/app/questions/new")
						.sessionAttr(TOKEN_ATTR_NAME, csrfToken)
						.param(csrfToken.getParameterName(), csrfToken.getToken())
						.param("title", questionToRequest.getTitle())
						.param("description", questionToRequest.getDescription()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_withoutCsrfToken_forbidden() throws Exception {
		Question questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();

		mockMvc.perform(
				post("/app/questions/new")
						.with(user("user").password("password"))
						.param("title", questionToRequest.getTitle())
						.param("description", questionToRequest.getDescription()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_simpleCase_success() throws Exception {
		Question questionToRequest = QuestionDataProvider.prepareGoodQuestionToRequest();

		String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";

		HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
		CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

		mockMvc.perform(
				post("/app/questions/new")
						.with(user("user").password("password"))
						.sessionAttr(TOKEN_ATTR_NAME, csrfToken)
						.param(csrfToken.getParameterName(), csrfToken.getToken())
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
		Question questionToRequest = QuestionDataProvider.prepareGoodQuestionWithEmptyDescriptionToRequest();

		String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";

		HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
		CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

		mockMvc.perform(
				post("/app/questions/new")
						.with(user("user").password("password"))
						.sessionAttr(TOKEN_ATTR_NAME, csrfToken)
						.param(csrfToken.getParameterName(), csrfToken.getToken())
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
		Question questionToRequest = QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest();

		String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";

		HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
		CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

		ModelAndView modelAndView = mockMvc.perform(
				post("/app/questions/new")
						.with(user("user").password("password"))
						.sessionAttr(TOKEN_ATTR_NAME, csrfToken)
						.param(csrfToken.getParameterName(), csrfToken.getToken())
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
		Question questionFromModel = (Question) modelAndView.getModel().get("question");
		Assertions.assertEquals(questionToRequest.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(questionToRequest.getDescription(), questionFromModel.getDescription());
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_emptyTitle_validationErrors() throws Exception {
		Question questionToRequest = QuestionDataProvider.prepareQuestionWithEmptyTitleToRequest();

		String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";

		HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
		CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

		ModelAndView modelAndView = mockMvc.perform(
				post("/app/questions/new")
						.with(user("user").password("password"))
						.sessionAttr(TOKEN_ATTR_NAME, csrfToken)
						.param(csrfToken.getParameterName(), csrfToken.getToken())
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
		Question questionFromModel = (Question) modelAndView.getModel().get("question");
		Assertions.assertEquals(questionToRequest.getTitle(), questionFromModel.getTitle());
		Assertions.assertEquals(questionToRequest.getDescription(), questionFromModel.getDescription());
	}

	@Test
	@WithAnonymousUser
	void downloadPdf_withAnonymousUser_redirectToLoginPage() throws Exception {
		String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";

		HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
		CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

		mockMvc.perform(
				get("/app/questions/pdf-export")
						.sessionAttr(TOKEN_ATTR_NAME, csrfToken)
						.param(csrfToken.getParameterName(), csrfToken.getToken()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void downloadPdf_withoutCsrfToken_success() throws Exception {

		mockMvc.perform(
				get("/app/questions/pdf-export")
						.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().string("Content-Disposition", "attachment; filename=\"Pytania.pdf\""))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithMockUser(username = "user")
	void downloadPdf_simpleCase_success() throws Exception {
		String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";

		HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
		CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

		mockMvc.perform(
				get("/app/questions/pdf-export")
						.with(user("user").password("password"))
						.sessionAttr(TOKEN_ATTR_NAME, csrfToken)
						.param(csrfToken.getParameterName(), csrfToken.getToken()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().string("Content-Disposition", "attachment; filename=\"Pytania.pdf\""))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithAnonymousUser
	void downloadExcel_withAnonymousUser_redirectToLoginPage() throws Exception {
		String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";

		HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
		CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

		mockMvc.perform(
				get("/app/questions/excel-export")
						.sessionAttr(TOKEN_ATTR_NAME, csrfToken)
						.param(csrfToken.getParameterName(), csrfToken.getToken()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void downloadExcel_withoutCsrfToken_success() throws Exception {
		mockMvc.perform(
				get("/app/questions/excel-export")
						.with(user("user").password("password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().string("Content-Disposition", "attachment; filename=\"Pytania.xlsx\""))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithMockUser(username = "user")
	void downloadExcel_simpleCase_success() throws Exception {
		String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";

		HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
		CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

		mockMvc.perform(
				get("/app/questions/excel-export")
						.with(user("user").password("password"))
						.sessionAttr(TOKEN_ATTR_NAME, csrfToken)
						.param(csrfToken.getParameterName(), csrfToken.getToken()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().string("Content-Disposition", "attachment; filename=\"Pytania.xlsx\""))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}
}