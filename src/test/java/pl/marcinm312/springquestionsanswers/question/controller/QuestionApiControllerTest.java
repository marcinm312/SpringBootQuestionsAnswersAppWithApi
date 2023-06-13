package pl.marcinm312.springquestionsanswers.question.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import pl.marcinm312.springquestionsanswers.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springquestionsanswers.config.security.SecurityMessagesConfig;
import pl.marcinm312.springquestionsanswers.config.security.jwt.JwtCreator;
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
import pl.marcinm312.springquestionsanswers.shared.mail.MailService;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.repository.TokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springquestionsanswers.user.service.UserManager;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class),
		@SpyBean(ExcelGenerator.class), @SpyBean(PdfGenerator.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
class QuestionApiControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private QuestionRepository questionRepository;

	@MockBean
	private UserRepo userRepo;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
	private final XmlMapper xmlMapper = new XmlMapper();

	private final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final UserEntity secondUser = UserDataProvider.prepareExampleSecondGoodUserWithEncodedPassword();
	private final UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();
	private final UserEntity userWithChangedPassword = UserDataProvider.prepareExampleGoodUserWithEncodedAndChangedPassword();

	@Value("${jwt.secret}")
	private String secret;

	@BeforeEach
	void setup() {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		given(questionRepository.getPaginatedQuestions(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"))))
				.willReturn(new PageImpl<>(QuestionDataProvider.prepareExampleQuestionsList()));
		given(questionRepository.getPaginatedQuestions(PageRequest.of(0, 5000, Sort.by(Sort.Direction.DESC, "id"))))
				.willReturn(new PageImpl<>(QuestionDataProvider.prepareExampleQuestionsList()));
		given(questionRepository.searchPaginatedQuestions("aaaa", PageRequest.of(0, 5,
				Sort.by(Sort.Direction.ASC, "id"))))
				.willReturn(new PageImpl<>(QuestionDataProvider.prepareExampleSearchedQuestionsList()));
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

	@ParameterizedTest(name = "{index} ''{2}''")
	@MethodSource("examplesOfQuestionsGetUrls")
	void getQuestions_jsonParameterized_success(String url, int expectedElements, String nameOfTestCase) throws Exception {

		String token = prepareToken("user", "password");

		String response = mockMvc.perform(get(url).header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		ObjectNode root = (ObjectNode) mapper.readTree(response);
		int amountOfElements = root.get("itemsList").size();
		Assertions.assertEquals(expectedElements, amountOfElements);
	}

	@ParameterizedTest(name = "{index} ''{2}''")
	@MethodSource("examplesOfQuestionsGetUrls")
	void getQuestions_xmlParameterized_success(String url, int expectedElements, String nameOfTestCase) throws Exception {

		String token = prepareToken("user", "password");

		String response = mockMvc.perform(get(url).header("Authorization", token).accept(MediaType.APPLICATION_XML))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/xml;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();

		Document document = DocumentHelper.parseText(response);
		List<Node> nodes = document.selectNodes("/ListPage/itemsList/itemsList");
		int amountOfElements = nodes.size();
		Assertions.assertEquals(expectedElements, amountOfElements);
	}

	private static Stream<Arguments> examplesOfQuestionsGetUrls() {
		return Stream.of(
				Arguments.of("/api/questions", 3,
						"getQuestions_simpleCase_success"),
				Arguments.of("/api/questions?keyword=aaaa&pageNo=-1&pageSize=0&sortField=TEXT&sortDirection=ASC", 1,
						"getQuestions_searchedQuestions_success"),
				Arguments.of("/api/questions?keyword=aaaa&pageNo=1&pageSize=0&sortField=TEXT&sortDirection=ASC", 1,
						"getQuestions_searchedQuestions_success"),
				Arguments.of("/api/questions?keyword=aaaa&pageNo=0&pageSize=5&sortField=TEXT&sortDirection=ASC", 1,
						"getQuestions_searchedQuestions_success"),
				Arguments.of("/api/questions?keyword=aaaa&pageNo=1&pageSize=5&sortField=TEXT&sortDirection=ASC", 1,
						"getQuestions_searchedQuestions_success"),
				Arguments.of("/api/questions?keyword=aaaa&pageNo=1&pageSize=5&sortField=ID&sortDirection=ASC", 1,
						"getQuestions_searchedQuestions_success"),
				Arguments.of("/api/questions?pageNo=1&pageSize=5&sortField=TEXT&sortDirection=DESC", 3,
						"getQuestions_paginatedQuestions_success"),
				Arguments.of("/api/questions?pageNo=1&pageSize=5000&sortField=TEXT&sortDirection=DESC", 3,
						"getQuestions_paginatedQuestions_success")
		);
	}

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("examplesOfTooLargePageSizeUrls")
	void limitExceeded_tooLargePageSize_badRequest(String url, String nameOfTestCase) throws Exception {

		String token = prepareToken("user", "password");

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
				Arguments.of("/api/questions?pageNo=1&pageSize=5001&sortField=TEXT&sortDirection=DESC",
						"getQuestions_tooLargePageSize_badRequest"),
				Arguments.of("/api/questions/file-export?fileType=PDF&pageNo=1&pageSize=5001&sortField=TEXT&sortDirection=DESC",
						"downloadPdf_tooLargePageSize_badRequest"),
				Arguments.of("/api/questions/file-export?fileType=EXCEL&pageNo=1&pageSize=5001&sortField=TEXT&sortDirection=DESC",
						"downloadExcel_tooLargePageSize_badRequest")
		);
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

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("examplesOfCreateQuestionGoodRequests")
	void createQuestion_goodRequestBody_success(QuestionCreateUpdate questionToRequest, String nameOfTestCase)
			throws Exception {

		String token = prepareToken("user", "password");

		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequest.getTitle(), questionToRequest.getDescription(), commonUser));
		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);
		String response = mockMvc.perform(
						post("/api/questions")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);
		Assertions.assertEquals(questionToRequest.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequest.getDescription(), responseQuestion.getDescription());

		verify(questionRepository, times(1)).save(any(QuestionEntity.class));
	}

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("examplesOfCreateQuestionGoodRequests")
	void createQuestion_goodXmlRequestBody_success(QuestionCreateUpdate questionToRequest, String nameOfTestCase)
			throws Exception {

		String token = prepareToken("user", "password");

		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequest.getTitle(), questionToRequest.getDescription(), commonUser));
		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);
		String response = mockMvc.perform(
						post("/api/questions")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_XML)
								.accept(MediaType.APPLICATION_XML)
								.content(xmlMapper.writeValueAsString(questionToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/xml;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = xmlMapper.readValue(response, QuestionGet.class);

		Assertions.assertEquals(questionToRequest.getTitle(), responseQuestion.getTitle());
		if (questionToRequest.getDescription() == null) {
			Assertions.assertEquals("", responseQuestion.getDescription());
		} else {
			Assertions.assertEquals(questionToRequest.getDescription(), responseQuestion.getDescription());
		}

		verify(questionRepository, times(1)).save(any(QuestionEntity.class));
	}

	private static Stream<Arguments> examplesOfCreateQuestionGoodRequests() {
		return Stream.of(
				Arguments.of(QuestionDataProvider.prepareGoodQuestionToRequest(),
						"createQuestion_simpleCase_success"),
				Arguments.of(QuestionDataProvider.prepareGoodQuestionWithNullDescriptionToRequest(),
						"createQuestion_nullDescription_success")
		);
	}

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("examplesOfCreateQuestionBadRequests")
	void createQuestion_incorrectQuestion_badRequest(QuestionCreateUpdate questionToRequest, String nameOfTestCase)
			throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(
						post("/api/questions")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	private static Stream<Arguments> examplesOfCreateQuestionBadRequests() {
		return Stream.of(
				Arguments.of(QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest(),
						"createQuestion_tooShortTitle_badRequest"),
				Arguments.of(QuestionDataProvider.prepareQuestionWithTooShortTitleAfterTrimToRequest(),
						"createQuestion_tooShortTitleAfterTrim_badRequest"),
				Arguments.of(QuestionDataProvider.prepareQuestionWithNullTitleToRequest(),
						"createQuestion_nullTitle_badRequest")
		);
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

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("examplesOfUpdateAnswerGoodRequests")
	void updateQuestion_goodQuestion_success(QuestionCreateUpdate questionToRequest, String nameOfTestCase)
			throws Exception {

		String token = prepareToken("user", "password");

		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequest.getTitle(), questionToRequest.getDescription(), commonUser));
		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);
		String response = mockMvc.perform(
						put("/api/questions/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		QuestionGet responseQuestion = mapper.readValue(response, QuestionGet.class);
		Assertions.assertEquals(questionToRequest.getTitle(), responseQuestion.getTitle());
		Assertions.assertEquals(questionToRequest.getDescription(), responseQuestion.getDescription());

		verify(questionRepository, times(1)).save(any(QuestionEntity.class));
	}

	private static Stream<Arguments> examplesOfUpdateAnswerGoodRequests() {
		return Stream.of(
				Arguments.of(QuestionDataProvider.prepareGoodQuestionToRequest(),
						"updateQuestion_userUpdatesHisOwnQuestion_success"),
				Arguments.of(QuestionDataProvider.prepareGoodQuestionWithNullDescriptionToRequest(),
						"updateQuestion_nullDescription_success")
		);
	}

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("examplesOfUpdateQuestionBadRequests")
	void updateQuestion_incorrectQuestion_badRequest(QuestionCreateUpdate questionToRequest, String nameOfTestCase)
			throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(
						put("/api/questions/1000")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(mapper.writeValueAsString(questionToRequest))
								.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());

		verify(questionRepository, never()).save(any(QuestionEntity.class));
	}

	private static Stream<Arguments> examplesOfUpdateQuestionBadRequests() {
		return Stream.of(
				Arguments.of(QuestionDataProvider.prepareQuestionWithTooShortTitleToRequest(),
						"updateQuestion_tooShortTitle_badRequest"),
				Arguments.of(QuestionDataProvider.prepareQuestionWithTooShortTitleAfterTrimToRequest(),
						"updateQuestion_tooShortTitleAfterTrim_badRequest"),
				Arguments.of(QuestionDataProvider.prepareQuestionWithNullTitleToRequest(),
						"updateQuestion_nullTitle_badRequest")
		);
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

		String token = prepareToken("admin", "password");

		QuestionCreateUpdate questionToRequestBody = QuestionDataProvider.prepareGoodQuestionToRequest();
		given(questionRepository.save(any(QuestionEntity.class)))
				.willReturn(new QuestionEntity(questionToRequestBody.getTitle(), questionToRequestBody.getDescription(), adminUser));
		given(userRepo.getUserFromAuthentication(any())).willReturn(adminUser);
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
				.willReturn(new QuestionEntity(questionToRequestBody.getTitle(), questionToRequestBody.getDescription(), secondUser));
		given(userRepo.getUserFromAuthentication(any())).willReturn(secondUser);
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
		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);
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
		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);

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

		String token = prepareToken("admin", "password");
		given(userRepo.getUserFromAuthentication(any())).willReturn(adminUser);

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
		given(userRepo.getUserFromAuthentication(any())).willReturn(secondUser);

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
		given(userRepo.getUserFromAuthentication(any())).willReturn(commonUser);

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
						get("/api/questions/file-export?fileType=PDF"))
				.andExpect(status().isUnauthorized());
	}

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("examplesOfDownloadPdfUrls")
	void downloadPdf_parameterized_success(String url, String nameOfTestCase) throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(get(url).header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"));
	}

	private static Stream<Arguments> examplesOfDownloadPdfUrls() {
		return Stream.of(
				Arguments.of("/api/questions/file-export?fileType=PDF",
						"downloadPdf_simpleCase_success"),
				Arguments.of("/api/questions/file-export?fileType=PDF&keyword=aaaa&pageNo=-1&pageSize=0&sortField=TEXT&sortDirection=ASC",
						"downloadPdf_searchedQuestions_success"),
				Arguments.of("/api/questions/file-export?fileType=PDF&keyword=aaaa&pageNo=1&pageSize=0&sortField=TEXT&sortDirection=ASC",
						"downloadPdf_searchedQuestions_success"),
				Arguments.of("/api/questions/file-export?fileType=PDF&keyword=aaaa&pageNo=0&pageSize=5&sortField=TEXT&sortDirection=ASC",
						"downloadPdf_searchedQuestions_success"),
				Arguments.of("/api/questions/file-export?fileType=PDF&keyword=aaaa&pageNo=1&pageSize=5&sortField=TEXT&sortDirection=ASC",
						"downloadPdf_searchedQuestions_success"),
				Arguments.of("/api/questions/file-export?fileType=PDF&keyword=aaaa&pageNo=1&pageSize=5&sortField=ID&sortDirection=ASC",
						"downloadPdf_searchedQuestions_success"),
				Arguments.of("/api/questions/file-export?fileType=PDF&pageNo=1&pageSize=5&sortField=TEXT&sortDirection=DESC",
						"downloadPdf_paginatedQuestions_success"),
				Arguments.of("/api/questions/file-export?fileType=PDF&pageNo=1&pageSize=5000&sortField=TEXT&sortDirection=DESC",
						"downloadPdf_paginatedQuestions_success")
		);
	}

	@Test
	void downloadExcel_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						get("/api/questions/file-export?fileType=EXCEL"))
				.andExpect(status().isUnauthorized());
	}

	@ParameterizedTest(name = "{index} ''{1}''")
	@MethodSource("examplesOfDownloadExcelUrls")
	void downloadExcel_parameterized_success(String url, String nameOfTestCase) throws Exception {

		String token = prepareToken("user", "password");

		mockMvc.perform(get(url).header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(header().string("Accept-Ranges", "bytes"));
	}

	private static Stream<Arguments> examplesOfDownloadExcelUrls() {
		return Stream.of(
				Arguments.of("/api/questions/file-export?fileType=EXCEL",
						"downloadExcel_simpleCase_success"),
				Arguments.of("/api/questions/file-export?fileType=EXCEL&keyword=aaaa&pageNo=-1&pageSize=0&sortField=TEXT&sortDirection=ASC",
						"downloadExcel_searchedQuestions_success"),
				Arguments.of("/api/questions/file-export?fileType=EXCEL&keyword=aaaa&pageNo=1&pageSize=0&sortField=TEXT&sortDirection=ASC",
						"downloadExcel_searchedQuestions_success"),
				Arguments.of("/api/questions/file-export?fileType=EXCEL&keyword=aaaa&pageNo=0&pageSize=5&sortField=TEXT&sortDirection=ASC",
						"downloadExcel_searchedQuestions_success"),
				Arguments.of("/api/questions/file-export?fileType=EXCEL&keyword=aaaa&pageNo=1&pageSize=5&sortField=TEXT&sortDirection=ASC",
						"downloadExcel_searchedQuestions_success"),
				Arguments.of("/api/questions/file-export?fileType=EXCEL&keyword=aaaa&pageNo=1&pageSize=5&sortField=ID&sortDirection=ASC",
						"downloadExcel_searchedQuestions_success"),
				Arguments.of("/api/questions/file-export?fileType=EXCEL&pageNo=1&pageSize=5&sortField=TEXT&sortDirection=DESC",
						"downloadExcel_paginatedQuestions_success"),
				Arguments.of("/api/questions/file-export?fileType=EXCEL&pageNo=1&pageSize=5000&sortField=TEXT&sortDirection=DESC",
						"downloadExcel_paginatedQuestions_success")
		);
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
