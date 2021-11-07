package pl.marcinm312.springdatasecurityex.question.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import pl.marcinm312.springdatasecurityex.config.security.jwt.JwtCreator;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.question.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.user.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.user.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.shared.mail.MailService;
import pl.marcinm312.springdatasecurityex.question.service.QuestionManager;
import pl.marcinm312.springdatasecurityex.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;
import pl.marcinm312.springdatasecurityex.question.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.config.security.utils.SessionUtils;
import pl.marcinm312.springdatasecurityex.shared.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.shared.file.PdfGenerator;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
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
@SpyBeans({@SpyBean(QuestionManager.class), @SpyBean(UserDetailsServiceImpl.class), @SpyBean(UserManager.class),
		@SpyBean(ExcelGenerator.class), @SpyBean(PdfGenerator.class),
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
class QuestionApiControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private QuestionRepository questionRepository;

	@MockBean
	private UserRepo userRepo;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper mapper = new ObjectMapper();

	private final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final UserEntity secondUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
	private final UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();
	private final UserEntity userWithChangedPassword = UserDataProvider.prepareExampleGoodUserWithEncodedAndChangedPassword();

	@Value("${jwt.secret}")
	private String secret;

	@BeforeEach
	void setup() {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		given(questionRepository.findAllByOrderByIdDesc())
				.willReturn(QuestionDataProvider.prepareExampleQuestionsList());
		given(questionRepository.findById(1000L)).willReturn(Optional.of(question));
		given(questionRepository.findById(2000L)).willReturn(Optional.empty());
		doNothing().when(questionRepository).delete(isA(QuestionEntity.class));

		given(userRepo.findById(commonUser.getId())).willReturn(Optional.of(commonUser));
		given(userRepo.findById(secondUser.getId())).willReturn(Optional.of(secondUser));
		given(userRepo.findById(adminUser.getId())).willReturn(Optional.of(adminUser));
		given(userRepo.findById(userWithChangedPassword.getId())).willReturn(Optional.of(userWithChangedPassword));

		given(userRepo.findByUsername(commonUser.getUsername())).willReturn(Optional.of(commonUser));
		given(userRepo.findByUsername(secondUser.getUsername())).willReturn(Optional.of(secondUser));
		given(userRepo.findByUsername(adminUser.getUsername())).willReturn(Optional.of(adminUser));
		given(userRepo.findByUsername(userWithChangedPassword.getUsername())).willReturn(Optional.of(userWithChangedPassword));

		given(userRepo.findByUsername("lalala")).willReturn(Optional.empty());

		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(this.webApplicationContext)
						.apply(springSecurity())
						.alwaysDo(print())
						.build();
	}

	@Test
	void getQuestions_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						get("/api/questions"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getQuestions_expiredToken_unauthorized() throws Exception {

		String token = prepareExpiredToken("user");

		mockMvc.perform(
						get("/api/questions")
								.header("Authorization", token))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getQuestions_tokenBeforePasswordChange_unauthorized() throws Exception {

		String token = prepareToken("user4", "password");

		mockMvc.perform(
						get("/api/questions")
								.header("Authorization", token))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getQuestions_tokenForNotExistingUser_unauthorized() throws Exception {

		String token = prepareTokenForNotExistingUser();

		mockMvc.perform(
						get("/api/questions")
								.header("Authorization", token))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getQuestions_tokenWithoutBearer_unauthorized() throws Exception {

		String token = prepareToken("user", "password").replace("Bearer ", "");

		mockMvc.perform(
						get("/api/questions")
								.header("Authorization", token))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getQuestions_incorrectBearerToken_unauthorized() throws Exception {

		mockMvc.perform(
						get("/api/questions")
								.header("Authorization", "Bearer aaaaaaaaaaaaa"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getQuestions_incorrectBearerToken2_unauthorized() throws Exception {

		mockMvc.perform(
						get("/api/questions")
								.header("Authorization", "Bearer aaaaaaaaaaaaa.bbbbb.ccccc"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getQuestions_simpleCase_success() throws Exception {

		String token = prepareToken("user", "password");

		String response = mockMvc.perform(
						get("/api/questions")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		QuestionGet[] responseQuestionList = mapper.readValue(response, QuestionGet[].class);
		int arrayExpectedSize = 3;
		int arrayResultSize = responseQuestionList.length;
		Assertions.assertEquals(arrayExpectedSize, arrayResultSize);
	}

	@Test
	void getQuestion_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						get("/api/questions/1000"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getQuestion_simpleCase_success() throws Exception {

		String token = prepareToken("user", "password");

		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		String response = mockMvc.perform(
						get("/api/questions/1000")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);

		Assertions.assertEquals(question.getId(), responseQuestion.getId());
		Assertions.assertEquals(question.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(question.getDescription(), responseQuestion.getDescription());
		Assertions.assertEquals(question.getUser().getUsername(), responseQuestion.getUser());
	}

	@Test
	void getQuestion_questionNotExists_notFound() throws Exception {

		String token = prepareToken("user", "password");

		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						get("/api/questions/2000")
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	@Test
	void createQuestion_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						post("/api/questions")
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(QuestionDataProvider.prepareGoodQuestionToRequest()))
								.characterEncoding("utf-8"))
				.andExpect(status().isUnauthorized());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void createQuestion_simpleCase_success() throws Exception {

		String token = prepareToken("user", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequestBody.getTitle(), questionToRequestBody.getDescription()));
		String response = mockMvc.perform(
						post("/api/questions")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);
		Assertions.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());

		verify(questionRepository, times(1)).save(any(QuestionEntity.class));
	}

	@Test
	void createQuestion_nullDescription_success() throws Exception {

		String token = prepareToken("user", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionWithNullDescriptionToRequest();
		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequestBody.getTitle(), questionToRequestBody.getDescription()));
		String response = mockMvc.perform(
						post("/api/questions")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);
		Assertions.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());

		verify(questionRepository, times(1)).save(any(QuestionEntity.class));
	}

	@Test
	void createQuestion_tooShortTitle_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest();
		mockMvc.perform(
						post("/api/questions")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void createQuestion_tooShortTitleAfterTrim_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareQuestionWithTooShortTitleAfterTrimToRequest();
		mockMvc.perform(
						post("/api/questions")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void createQuestion_nullTitle_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareQuestionWithNullTitleToRequest();
		mockMvc.perform(
						post("/api/questions")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void createQuestion_nullBody_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(
						post("/api/questions")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content("")
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void updateQuestion_withAnonymousUser_unauthorized() throws Exception {
		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		mockMvc.perform(
						put("/api/questions/1000")
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isUnauthorized());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void updateQuestion_userUpdatesHisOwnQuestion_success() throws Exception {

		String token = prepareToken("user", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequestBody.getTitle(), questionToRequestBody.getDescription()));
		String response = mockMvc.perform(
						put("/api/questions/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);
		Assertions.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());

		verify(questionRepository, times(1)).save(any(QuestionEntity.class));
	}

	@Test
	void updateQuestion_nullDescription_success() throws Exception {

		String token = prepareToken("user", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionWithNullDescriptionToRequest();
		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequestBody.getTitle(), questionToRequestBody.getDescription()));
		String response = mockMvc.perform(
						put("/api/questions/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);
		Assertions.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());

		verify(questionRepository, times(1)).save(any(QuestionEntity.class));
	}

	@Test
	void updateQuestion_tooShortTitle_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest();
		mockMvc.perform(
						put("/api/questions/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void updateQuestion_nullTitle_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareQuestionWithNullTitleToRequest();
		mockMvc.perform(
						put("/api/questions/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void updateQuestion_nullBody_badRequest() throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(
						put("/api/questions/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content("")
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void updateQuestion_administratorUpdatesAnotherUsersQuestion_success() throws Exception {

		String token = prepareToken("administrator", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequestBody.getTitle(), questionToRequestBody.getDescription()));
		String response = mockMvc.perform(
						put("/api/questions/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);
		Assertions.assertEquals(questionToRequestBody.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequestBody.getDescription(), responseQuestion.getDescription());

		verify(questionRepository, times(1)).save(any(QuestionEntity.class));
	}

	@Test
	void updateQuestion_userUpdatesAnotherUsersQuestion_forbidden() throws Exception {

		String token = prepareToken("user2", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequestBody.getTitle(), questionToRequestBody.getDescription()));
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						put("/api/questions/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isForbidden())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Brak uprawnień do wykonania operacji!";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void updateQuestion_questionNotExists_notFound() throws Exception {

		String token = prepareToken("user", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						put("/api/questions/2000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequestBody))
								.characterEncoding("utf-8"))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	@Test
	void deleteQuestion_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						delete("/api/questions/1000"))
				.andExpect(status().isUnauthorized());

		verify(questionRepository, never()).delete(any(QuestionEntity.class));
	}

	@Test
	void deleteQuestion_userDeletesHisOwnQuestion_success() throws Exception {

		String token = prepareToken("user", "password");

		String response = mockMvc.perform(
						delete("/api/questions/1000")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Assertions.assertEquals("true", response);

		verify(questionRepository, times(1)).delete(any(QuestionEntity.class));
	}

	@Test
	void deleteQuestion_administratorDeletesAnotherUsersQuestion_success() throws Exception {

		String token = prepareToken("administrator", "password");

		String response = mockMvc.perform(
						delete("/api/questions/1000")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Assertions.assertEquals("true", response);

		verify(questionRepository, times(1)).delete(any(QuestionEntity.class));
	}

	@Test
	void deleteQuestion_userDeletesAnotherUsersQuestion_forbidden() throws Exception {

		String token = prepareToken("user2", "password");

		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						delete("/api/questions/1000")
								.header("Authorization", token))
				.andExpect(status().isForbidden())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Brak uprawnień do wykonania operacji!";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(questionRepository, never()).delete(any(QuestionEntity.class));
	}

	@Test
	void deleteQuestion_questionNotExists_notFound() throws Exception {

		String token = prepareToken("user", "password");

		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						delete("/api/questions/2000")
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Nie znaleziono pytania o id: 2000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);

		verify(questionRepository, never()).delete(any(QuestionEntity.class));
	}

	@Test
	void downloadPdf_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						get("/api/questions/pdf-export"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void downloadPdf_simpleCase_success() throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(
						get("/api/questions/pdf-export")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"));
	}

	@Test
	void downloadExcel_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						get("/api/questions/excel-export"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void downloadExcel_simpleCase_success() throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(
						get("/api/questions/excel-export")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"));
	}

	private String prepareToken(String username, String password) throws Exception {
		return mockMvc.perform(post("/api/login")
						.content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
						.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(header().exists("Authorization"))
				.andReturn().getResponse().getHeader("Authorization");
	}

	private String prepareTokenForNotExistingUser() {
		return "Bearer " + JwtCreator.createJWT("lalala", 60000, secret.getBytes());
	}

	private String prepareExpiredToken(String username) {
		return "Bearer " + JwtCreator.createJWT(username, - 60000, secret.getBytes());
	}
}