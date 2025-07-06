package pl.marcinm312.springquestionsanswers.mail.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springquestionsanswers.config.async.AsyncConfig;
import pl.marcinm312.springquestionsanswers.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springquestionsanswers.config.security.SecurityMessagesConfig;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springquestionsanswers.mail.exception.RuntimeMailException;
import pl.marcinm312.springquestionsanswers.mail.model.MailEntity;
import pl.marcinm312.springquestionsanswers.mail.model.dto.MailGet;
import pl.marcinm312.springquestionsanswers.mail.model.dto.MailRetryResult;
import pl.marcinm312.springquestionsanswers.mail.repository.MailRepository;
import pl.marcinm312.springquestionsanswers.mail.service.MailService;
import pl.marcinm312.springquestionsanswers.mail.testdataprovider.MailDataProvider;
import pl.marcinm312.springquestionsanswers.shared.testdataprovider.JwtProvider;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MailRetryApiController.class)
@ComponentScan(basePackageClasses = MailRetryApiController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = MailRetryApiController.class)
		})
@SpyBeans({@SpyBean(UserDetailsServiceImpl.class), @SpyBean(RestAuthenticationSuccessHandler.class),
		@SpyBean(RestAuthenticationFailureHandler.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class, AsyncConfig.class})
class MailRetryApiControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private JavaMailSender javaMailSender;

	@MockBean
	private MailRepository mailRepository;

	@MockBean
	private UserRepo userRepo;

	@SpyBean
	private MailService mailService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

	private final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();

	@BeforeEach
	void setUp() {

		MimeMessage mimeMessage = new MimeMessage((Session) null);
		given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);

		given(mailRepository.findAll()).willReturn(MailDataProvider.prepareExampleMailsList());
		given(mailRepository.findById(1000L)).willReturn(Optional.of(MailDataProvider.prepareExampleGoodMail()));
		given(mailRepository.findById(1002L)).willReturn(Optional.of(MailDataProvider.prepareExampleMailWithError()));
		given(mailRepository.findById(5000L)).willReturn(Optional.empty());

		given(userRepo.findById(commonUser.getId())).willReturn(Optional.of(commonUser));
		given(userRepo.findById(adminUser.getId())).willReturn(Optional.of(adminUser));

		given(userRepo.findByUsername(commonUser.getUsername())).willReturn(Optional.of(commonUser));
		given(userRepo.findByUsername(adminUser.getUsername())).willReturn(Optional.of(adminUser));

		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(this.webApplicationContext)
						.apply(springSecurity())
						.alwaysDo(print())
						.build();
	}

	@Test
	void sendMailAsync_simpleCase_success() throws ExecutionException, InterruptedException {

		Future<Boolean> resultFuture = mailService
				.sendMailAsync("aaa@abc.com", "Test maila", "Test maila", false);
		await().atMost(1, TimeUnit.SECONDS).until(() -> (resultFuture.isDone() || resultFuture.isCancelled()));
		Assertions.assertTrue(resultFuture.isDone());
		Assertions.assertTrue(resultFuture.get());
		verify(javaMailSender, times(1)).send(any(MimeMessage.class));
		verify(mailRepository, never()).save(any(MailEntity.class));
	}

	@Test
	void sendMailAsync_errorWhileSendingEmail_try3TimesAndThrowError() {

		doThrow(new RuntimeException("Mail Exception")).when(javaMailSender).send(any(MimeMessage.class));
		Future<Boolean> resultFuture = mailService
				.sendMailAsync("aaa@abc.com", "Test maila", "Test maila", false);
		await().atMost(5, TimeUnit.SECONDS).until(() -> (resultFuture.isDone() || resultFuture.isCancelled()));
		Assertions.assertTrue(resultFuture.isDone());
		Assertions.assertThrows(ExecutionException.class, resultFuture::get);
		verify(javaMailSender, times(3)).send(any(MimeMessage.class));
		verify(mailRepository, times(1)).save(any(MailEntity.class));
	}

	@Test
	void getMailsToRetry_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(get("/api/admin/mailsToRetry"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getMailsToRetry_withCommonUser_forbidden() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(get("/api/admin/mailsToRetry")
						.header("Authorization", token))
				.andExpect(status().isForbidden());
	}

	@Test
	void getMailsToRetry_simpleCase_success() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("admin", "password");
		String response = mockMvc.perform(get("/api/admin/mailsToRetry")
						.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		JsonNode root = mapper.readTree(response);
		int amountOfElements = root.size();
		Assertions.assertEquals(3, amountOfElements);
	}

	@Test
	void getOneMailToRetry_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(get("/api/admin/mailsToRetry/1000"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getOneMailToRetry_withCommonUser_forbidden() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(get("/api/admin/mailsToRetry/1000")
						.header("Authorization", token))
				.andExpect(status().isForbidden());
	}

	@Test
	void getOneMailToRetry_simpleCase_success() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("admin", "password");
		String response = mockMvc.perform(get("/api/admin/mailsToRetry/1000")
						.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		MailGet responseMail = mapper.readValue(response, MailGet.class);
		MailEntity expectedMail = MailDataProvider.prepareExampleGoodMail();
		Assertions.assertEquals(expectedMail.getId(), responseMail.getId());
		Assertions.assertEquals(expectedMail.getSubject(), responseMail.getSubject());
		Assertions.assertEquals(expectedMail.getEmailRecipient(), responseMail.getTo());
		Assertions.assertEquals(expectedMail.isHtmlContent(), responseMail.isHtmlContent());
	}

	@Test
	void getOneMailToRetry_mailNotExists_notFound() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("admin", "password");
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						get("/api/admin/mailsToRetry/5000")
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "No email found for id: 5000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	@Test
	void retryAllMails_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(post("/api/admin/mailsToRetry"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void retryAllMails_withCommonUser_forbidden() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(post("/api/admin/mailsToRetry")
						.header("Authorization", token))
				.andExpect(status().isForbidden());
	}

	@Test
	void retryAllMails_2goodMailsAnd1NullMail_2ProcessedSuccessfullyAnd1Error() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("admin", "password");
		String response = mockMvc.perform(post("/api/admin/mailsToRetry")
						.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();
		MailRetryResult mailRetryResult = mapper.readValue(response, MailRetryResult.class);

		Assertions.assertEquals(2, mailRetryResult.getProcessedSuccessfully());
		Assertions.assertEquals(1, mailRetryResult.getProcessedWithErrors());
		Assertions.assertEquals(3, mailRetryResult.getMailsToProcess());
	}

	@Test
	void retryOneMail_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(post("/api/admin/mailsToRetry/1000"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void retryOneMail_withCommonUser_forbidden() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(post("/api/admin/mailsToRetry/1000")
						.header("Authorization", token))
				.andExpect(status().isForbidden());
	}

	@Test
	void retryOneMail_simpleCase_success() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("admin", "password");
		String response = mockMvc.perform(post("/api/admin/mailsToRetry/1000")
						.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		Assertions.assertEquals("true", response);
	}

	@Test
	void retryOneMail_mailWithError_internalServerError() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("admin", "password");
		Exception exception = mockMvc.perform(post("/api/admin/mailsToRetry/1002")
						.header("Authorization", token))
				.andExpect(status().isInternalServerError())
				.andReturn().getResolvedException();

		Assertions.assertInstanceOf(RuntimeMailException.class, exception);
	}

	@Test
	void retryOneMail_mailNotExists_notFound() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("admin", "password");
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						post("/api/admin/mailsToRetry/5000")
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "No email found for id: 5000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	@Test
	void deleteOneMail_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(delete("/api/admin/mailsToRetry/1000"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deleteOneMail_withCommonUser_forbidden() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(delete("/api/admin/mailsToRetry/1000")
						.header("Authorization", token))
				.andExpect(status().isForbidden());
	}

	@Test
	void deleteOneMail_simpleCase_success() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("admin", "password");
		String response = mockMvc.perform(delete("/api/admin/mailsToRetry/1000")
						.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		Assertions.assertEquals("true", response);
	}

	@Test
	void deleteOneMail_mailNotExists_notFound() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("admin", "password");
		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						delete("/api/admin/mailsToRetry/5000")
								.header("Authorization", token))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "No email found for id: 5000";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}
}
