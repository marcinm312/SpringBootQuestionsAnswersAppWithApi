package pl.marcinm312.springdatasecurityex.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.mock.mockito.SpyBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springdatasecurityex.config.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(QuestionApiController.class)
@ComponentScan(basePackageClasses = QuestionApiController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = QuestionApiController.class)
		})
@SpyBeans({@SpyBean(QuestionManager.class), @SpyBean(ExcelGenerator.class), @SpyBean(PdfGenerator.class)})
@Import({MultiHttpSecurityCustomConfig.ApiWebSecurityConfigurationAdapter.class})
class QuestionApiControllerTest {

	private MockMvc mockMvc;

	@MockBean
	QuestionRepository questionRepository;

	@MockBean
	UserManager userManager;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper mapper = new ObjectMapper();

	@BeforeEach
	void setup() {
		Question question = QuestionDataProvider.prepareExampleQuestion();
		given(questionRepository.findAllByOrderByIdDesc())
				.willReturn(QuestionDataProvider.prepareExampleQuestionsList());
		given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
		given(questionRepository.findById(2000L)).willReturn(Optional.empty());
		doNothing().when(questionRepository).delete(isA(Question.class));
		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(this.webApplicationContext)
						.apply(springSecurity())
						.alwaysDo(print())
						.build();
	}

	@Test
	@WithAnonymousUser
	void getQuestions_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
				get("/api/questions"))
				.andExpect(status().isUnauthorized())
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void getQuestions_simpleCase_success() throws Exception {
		String response = mockMvc.perform(
				get("/api/questions")
						.with(httpBasic("user", "password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		Question[] responseQuestionList = mapper.readValue(response, Question[].class);
		int arrayExpectedSize = 3;
		int arrayResultSize = responseQuestionList.length;
		Assertions.assertEquals(arrayExpectedSize, arrayResultSize);
	}

	@Test
	@WithAnonymousUser
	void getQuestion_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
				get("/api/questions/1000"))
				.andExpect(status().isUnauthorized())
				.andExpect(unauthenticated()).andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void getQuestion_simpleCase_success() throws Exception {
		Question question = QuestionDataProvider.prepareExampleQuestion();
		String expectedTitle = question.getTitle();
		String expectedDescription = question.getDescription();
		String response = mockMvc.perform(
				get("/api/questions/1000")
						.with(httpBasic("user", "password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		Question responseQuestion = mapper.readValue(response, Question.class);

		Assertions.assertEquals(expectedTitle, responseQuestion.getTitle());
		Assertions.assertEquals(expectedDescription, responseQuestion.getDescription());
	}

	@Test
	@WithMockUser(username = "user")
	void getQuestion_questionNotExists_notFound() throws Exception {
		given(questionRepository.findById(2000L)).willReturn(Optional.empty());
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
				get("/api/questions/2000")
						.with(httpBasic("user", "password")))
				.andExpect(status().isNotFound())
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Question not found with id 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	@Test
	@WithAnonymousUser
	void createQuestion_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
				post("/api/questions")
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(QuestionDataProvider.prepareGoodQuestionToRequest()))
						.characterEncoding("utf-8"))
				.andExpect(status().isUnauthorized())
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_simpleCase_success() throws Exception {
		given(userManager.getUserByAuthentication(any(Authentication.class)))
				.willReturn(UserDataProvider.prepareExampleGoodUser());
		Question questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(Question.class))).willReturn(questionToRequestBody);
		String response = mockMvc.perform(
				post("/api/questions")
						.with(httpBasic("user", "password"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(questionToRequestBody))
						.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		Question responseQuestion = mapper.readValue(response, Question.class);
		Assertions.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_nullDescription_success() throws Exception {
		given(userManager.getUserByAuthentication(any(Authentication.class)))
				.willReturn(UserDataProvider.prepareExampleGoodUser());
		Question questionToRequestBody = QuestionDataProvider.prepareGoodQuestionWithNullDescriptionToRequest();
		given(questionRepository.save(any(Question.class))).willReturn(questionToRequestBody);
		String response = mockMvc.perform(
				post("/api/questions")
						.with(httpBasic("user", "password"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(questionToRequestBody))
						.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		Question responseQuestion = mapper.readValue(response, Question.class);
		Assertions.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_tooShortTitle_badRequest() throws Exception {
		given(userManager.getUserByAuthentication(any(Authentication.class)))
				.willReturn(UserDataProvider.prepareExampleGoodUser());
		mockMvc.perform(
				post("/api/questions")
						.with(httpBasic("user", "password"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest()))
						.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_nullTitle_badRequest() throws Exception {
		given(userManager.getUserByAuthentication(any(Authentication.class)))
				.willReturn(UserDataProvider.prepareExampleGoodUser());
		mockMvc.perform(
				post("/api/questions")
						.with(httpBasic("user", "password"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(QuestionDataProvider.prepareQuestionWithNullTitleToRequest()))
						.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_nullBody_badRequest() throws Exception {
		given(userManager.getUserByAuthentication(any(Authentication.class)))
				.willReturn(UserDataProvider.prepareExampleGoodUser());
		mockMvc.perform(
				post("/api/questions")
						.with(httpBasic("user", "password"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("")
						.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithAnonymousUser
	void deleteQuestion_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
				delete("/api/questions/1000"))
				.andExpect(status().isUnauthorized())
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void deleteQuestion_userDeletesHisOwnQuestion_success() throws Exception {
		given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(UserDataProvider.prepareExampleGoodUser());
		String response = mockMvc.perform(
				delete("/api/questions/1000")
						.with(httpBasic("user", "password")))
				.andExpect(status().isOk())
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		Assertions.assertEquals("true", response);
	}

	@Test
	@WithMockUser(username = "administrator", roles = {"ADMIN"})
	void deleteQuestion_administratorDeletesAnotherUsersQuestion_success() throws Exception {
		given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(UserDataProvider.prepareExampleGoodAdministrator());
		String response = mockMvc.perform(
				delete("/api/questions/1000")
						.with(httpBasic("administrator", "password")))
				.andExpect(status().isOk())
				.andExpect(authenticated().withUsername("administrator").withRoles("ADMIN"))
				.andReturn().getResponse().getContentAsString();

		Assertions.assertEquals("true", response);
	}

	@Test
	@WithMockUser(username = "user2")
	void deleteQuestion_userDeletesAnotherUsersQuestion_forbidden() throws Exception {
		given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(UserDataProvider.prepareExampleSecondGoodUser());
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
				delete("/api/questions/1000")
						.with(httpBasic("user2", "password")))
				.andExpect(status().isForbidden())
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Change not allowed!";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	@Test
	@WithMockUser(username = "user")
	void deleteQuestion_questionNotExists_notFound() throws Exception {
		given(userManager.getUserByAuthentication(any(Authentication.class))).willReturn(UserDataProvider.prepareExampleGoodUser());
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
				delete("/api/questions/2000")
						.with(httpBasic("user", "password")))
				.andExpect(status().isNotFound())
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Question not found with id 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	@Test
	@WithAnonymousUser
	void downloadPdf_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
				get("/api/questions/pdf-export"))
				.andExpect(status().isUnauthorized())
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void downloadPdf_simpleCase_success() throws Exception {
		mockMvc.perform(
				get("/api/questions/pdf-export")
						.with(httpBasic("user", "password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().string("Content-Disposition", "attachment; filename=\"Pytania.pdf\""))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	@WithAnonymousUser
	void downloadExcel_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
				get("/api/questions/excel-export"))
				.andExpect(status().isUnauthorized())
				.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser(username = "user")
	void downloadExcel_simpleCase_success() throws Exception {
		mockMvc.perform(
				get("/api/questions/excel-export")
						.with(httpBasic("user", "password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().string("Content-Disposition", "attachment; filename=\"Pytania.xlsx\""))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}
}
