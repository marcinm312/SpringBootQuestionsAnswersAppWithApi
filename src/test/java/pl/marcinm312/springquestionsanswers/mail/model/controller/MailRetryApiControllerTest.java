package pl.marcinm312.springquestionsanswers.mail.model.controller;

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
import pl.marcinm312.springquestionsanswers.mail.model.MailEntity;
import pl.marcinm312.springquestionsanswers.mail.repository.MailRepository;
import pl.marcinm312.springquestionsanswers.mail.service.MailService;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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

	private MimeMessage mimeMessage;

	@BeforeEach
	void setUp() {

		mimeMessage = new MimeMessage((Session) null);
		given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);

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
}