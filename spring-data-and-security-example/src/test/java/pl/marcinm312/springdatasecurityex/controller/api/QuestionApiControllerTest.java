package pl.marcinm312.springdatasecurityex.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
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

import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
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
@MockBeans({@MockBean(TokenRepo.class), @MockBean(MailService.class), @MockBean(SessionUtils.class)})
@SpyBeans({@SpyBean(QuestionManager.class), @SpyBean(ExcelGenerator.class), @SpyBean(PdfGenerator.class),
		@SpyBean(UserDetailsServiceImpl.class), @SpyBean(UserManager.class)})
@Import({MultiHttpSecurityCustomConfig.class})
class QuestionApiControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private QuestionRepository questionRepository;

	@MockBean
	private UserRepo userRepo;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper mapper = new ObjectMapper();

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

		QuestionGet[] responseQuestionList = mapper.readValue(response, QuestionGet[].class);
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
		String response = mockMvc.perform(
						get("/api/questions/1000")
								.with(httpBasic("user", "password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);

		Assertions.assertEquals(question.getId(), responseQuestion.getId());
		Assertions.assertEquals(question.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(question.getDescription(), responseQuestion.getDescription());
		Assertions.assertEquals(question.getUser().getUsername(), responseQuestion.getUser());
	}

	@Test
	@WithMockUser(username = "user")
	void getQuestion_questionNotExists_notFound() throws Exception {
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

		verify(questionRepository, never()).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_simpleCase_success() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(Question.class)))
				.willReturn(new Question(questionToRequestBody.getTitle(), questionToRequestBody.getDescription()));
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

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);
		Assertions.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());

		verify(questionRepository, times(1)).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_nullDescription_success() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionWithNullDescriptionToRequest();
		given(questionRepository.save(any(Question.class)))
				.willReturn(new Question(questionToRequestBody.getTitle(), questionToRequestBody.getDescription()));
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

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);
		Assertions.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());

		verify(questionRepository, times(1)).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_tooShortTitle_badRequest() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest();
		mockMvc.perform(
						post("/api/questions")
								.with(httpBasic("user", "password"))
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(questionRepository, never()).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_nullTitle_badRequest() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareQuestionWithNullTitleToRequest();
		mockMvc.perform(
						post("/api/questions")
								.with(httpBasic("user", "password"))
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(questionRepository, never()).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void createQuestion_nullBody_badRequest() throws Exception {
		mockMvc.perform(
						post("/api/questions")
								.with(httpBasic("user", "password"))
								.contentType(MediaType.APPLICATION_JSON)
								.content("")
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(questionRepository, never()).save(any(Question.class));
	}

	@Test
	@WithAnonymousUser
	void updateQuestion_withAnonymousUser_unauthorized() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		mockMvc.perform(
						put("/api/questions/1000")
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isUnauthorized())
				.andExpect(unauthenticated());

		verify(questionRepository, never()).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void updateQuestion_userUpdatesHisOwnQuestion_success() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(Question.class)))
				.willReturn(new Question(questionToRequestBody.getTitle(), questionToRequestBody.getDescription()));
		String response = mockMvc.perform(
						put("/api/questions/1000")
								.with(httpBasic("user", "password"))
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);
		Assertions.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());

		verify(questionRepository, times(1)).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void updateQuestion_nullDescription_success() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionWithNullDescriptionToRequest();
		given(questionRepository.save(any(Question.class)))
				.willReturn(new Question(questionToRequestBody.getTitle(), questionToRequestBody.getDescription()));
		String response = mockMvc.perform(
						put("/api/questions/1000")
								.with(httpBasic("user", "password"))
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);
		Assertions.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());

		verify(questionRepository, times(1)).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void updateQuestion_tooShortTitle_badRequest() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest();
				mockMvc.perform(
						put("/api/questions/1000")
								.with(httpBasic("user", "password"))
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(questionRepository, never()).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void updateQuestion_nullTitle_badRequest() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareQuestionWithNullTitleToRequest();
		mockMvc.perform(
						put("/api/questions/1000")
								.with(httpBasic("user", "password"))
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(questionRepository, never()).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void updateQuestion_nullBody_badRequest() throws Exception {
		mockMvc.perform(
						put("/api/questions/1000")
								.with(httpBasic("user", "password"))
								.contentType(MediaType.APPLICATION_JSON)
								.content("")
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest())
				.andExpect(authenticated().withUsername("user").withRoles("USER"));

		verify(questionRepository, never()).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "administrator", roles = {"ADMIN"})
	void updateQuestion_administratorUpdatesAnotherUsersQuestion_success() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(Question.class)))
				.willReturn(new Question(questionToRequestBody.getTitle(), questionToRequestBody.getDescription()));
		String response = mockMvc.perform(
						put("/api/questions/1000")
								.with(httpBasic("administrator", "password"))
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(authenticated().withUsername("administrator").withRoles("ADMIN"))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);
		Assertions.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());

		verify(questionRepository, times(1)).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void updateQuestion_userUpdatesAnotherUsersQuestion_forbidden() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(Question.class)))
				.willReturn(new Question(questionToRequestBody.getTitle(), questionToRequestBody.getDescription()));
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						put("/api/questions/1000")
								.with(httpBasic("user2", "password"))
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isForbidden())
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Change not allowed!";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(questionRepository, never()).save(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void updateQuestion_questionNotExists_notFound() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();

		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						put("/api/questions/2000")
								.with(httpBasic("user", "password"))
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isNotFound())
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Question not found with id 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(questionRepository, never()).save(any(Question.class));
	}

	@Test
	@WithAnonymousUser
	void deleteQuestion_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						delete("/api/questions/1000"))
				.andExpect(status().isUnauthorized())
				.andExpect(unauthenticated());

		verify(questionRepository, never()).delete(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void deleteQuestion_userDeletesHisOwnQuestion_success() throws Exception {
		String response = mockMvc.perform(
						delete("/api/questions/1000")
								.with(httpBasic("user", "password")))
				.andExpect(status().isOk())
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		Assertions.assertEquals("true", response);

		verify(questionRepository, times(1)).delete(any(Question.class));
	}

	@Test
	@WithMockUser(username = "administrator", roles = {"ADMIN"})
	void deleteQuestion_administratorDeletesAnotherUsersQuestion_success() throws Exception {
		String response = mockMvc.perform(
						delete("/api/questions/1000")
								.with(httpBasic("administrator", "password")))
				.andExpect(status().isOk())
				.andExpect(authenticated().withUsername("administrator").withRoles("ADMIN"))
				.andReturn().getResponse().getContentAsString();

		Assertions.assertEquals("true", response);

		verify(questionRepository, times(1)).delete(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user2")
	void deleteQuestion_userDeletesAnotherUsersQuestion_forbidden() throws Exception {
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						delete("/api/questions/1000")
								.with(httpBasic("user2", "password")))
				.andExpect(status().isForbidden())
				.andExpect(authenticated().withUsername("user2").withRoles("USER"))
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Change not allowed!";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(questionRepository, never()).delete(any(Question.class));
	}

	@Test
	@WithMockUser(username = "user")
	void deleteQuestion_questionNotExists_notFound() throws Exception {
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						delete("/api/questions/2000")
								.with(httpBasic("user", "password")))
				.andExpect(status().isNotFound())
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Question not found with id 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(questionRepository, never()).delete(any(Question.class));
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
				.andExpect(header().exists("Content-Disposition"))
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
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"))
				.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}
}
