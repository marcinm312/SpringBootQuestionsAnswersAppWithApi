package pl.marcinm312.springquestionsanswers.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pl.marcinm312.springquestionsanswers.answer.repository.AnswerRepository;
import pl.marcinm312.springquestionsanswers.mail.service.MailService;
import pl.marcinm312.springquestionsanswers.question.repository.QuestionRepository;
import pl.marcinm312.springquestionsanswers.user.model.ActivationTokenEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserCreate;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserGet;
import pl.marcinm312.springquestionsanswers.user.repository.ActivationTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.MailChangeTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.FirstUserCreator;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.ActivationTokenDataProvider;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.SYSTEM_OUT, printOnlyOnFailure = false)
@EnableAutoConfiguration(exclude = {
		org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration.class,
		org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
		org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
		org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
})
@MockitoBean(types = {AnswerRepository.class, QuestionRepository.class, MailChangeTokenRepo.class,
		FirstUserCreator.class})
class UserRegistrationApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MailService mailService;

	@MockitoBean
	private UserRepo userRepo;

	@MockitoBean
	private ActivationTokenRepo activationTokenRepo;


	private final ObjectMapper mapper = new ObjectMapper();


	@ParameterizedTest
	@MethodSource("examplesOfUserRegistrationGoodRequests")
	void createUser_goodUser_success(UserCreate userToRequest) throws Exception {

		UserEntity user = UserEntity.builder()
				.username(userToRequest.getUsername())
				.password(userToRequest.getPassword())
				.email(userToRequest.getEmail())
				.build();
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());
		given(activationTokenRepo.save(any(ActivationTokenEntity.class))).willReturn(new ActivationTokenEntity("123456789", user));
		given(userRepo.save(any(UserEntity.class))).willReturn(user);

		String response = mockMvc.perform(
						post("/api/registration")
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		UserGet responseUser = mapper.readValue(response, UserGet.class);
		Assertions.assertEquals(userToRequest.getUsername(), responseUser.getUsername());
		Assertions.assertEquals(userToRequest.getEmail(), responseUser.getEmail());
		verify(userRepo, times(1)).save(any(UserEntity.class));
		verify(mailService, times(1)).sendMailAsync(any(String.class), any(String.class),
				any(String.class), eq(true));
	}

	private static Stream<Arguments> examplesOfUserRegistrationGoodRequests() {

		return Stream.of(
				Arguments.of(UserDataProvider.prepareGoodUserToRequest()),
				Arguments.of(UserDataProvider.prepareUserWithSpacesInPasswordToRequest()),
				Arguments.of(UserDataProvider.prepareGoodUserWithActivationUrlToRequest()),
				Arguments.of(UserDataProvider.prepareGoodUserWithIncorrectActivationUrlToRequest())
		);
	}

	@ParameterizedTest
	@MethodSource("examplesOfUserRegistrationBadRequests")
	void createUser_incorrectUser_validationError(UserCreate userToRequest, UserEntity foundUserWithTheSameLogin)
			throws Exception {

		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.ofNullable(foundUserWithTheSameLogin));

		mockMvc.perform(
						post("/api/registration")
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(mapper.writeValueAsString(userToRequest)))
				.andExpect(status().isBadRequest());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(activationTokenRepo, never()).save(any(ActivationTokenEntity.class));
		verify(mailService, never()).sendMailAsync(any(String.class), any(String.class), any(String.class), eq(true));
	}

	private static Stream<Arguments> examplesOfUserRegistrationBadRequests() {

		UserEntity existingUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		return Stream.of(
				Arguments.of(UserDataProvider.prepareUserWithConfirmPasswordErrorToRequest(), null),
				Arguments.of(UserDataProvider.prepareUserWithTooShortLoginAfterTrimToRequest(), null),
				Arguments.of(UserDataProvider.prepareGoodUserToRequest(), existingUser),
				Arguments.of(UserDataProvider.prepareIncorrectUserToRequest(), null),
				Arguments.of(UserDataProvider.prepareEmptyUserToRequest(), null)
		);
	}

	@Test
	void createUser_nullBody_validationError() throws Exception {

		mockMvc.perform(
						post("/api/registration")
								.contentType(MediaType.APPLICATION_JSON)
								.characterEncoding("utf-8")
								.content(""))
				.andExpect(status().isBadRequest());

		verify(userRepo, never()).save(any(UserEntity.class));
		verify(activationTokenRepo, never()).save(any(ActivationTokenEntity.class));
		verify(mailService, never()).sendMailAsync(any(String.class), any(String.class), any(String.class), eq(true));
	}

	@Test
	void activateUser_simpleCase_userActivated() throws Exception {

		ActivationTokenEntity foundToken = ActivationTokenDataProvider.prepareExampleToken();
		String exampleExistingTokenValue = "123456-123-123-1234";
		given(activationTokenRepo.findByValue(exampleExistingTokenValue)).willReturn(Optional.of(foundToken));
		given(userRepo.save(any(UserEntity.class))).willReturn(foundToken.getUser());

		mockMvc.perform(
						put("/api/token?value=" + exampleExistingTokenValue))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(unauthenticated());

		verify(activationTokenRepo, times(1)).delete(foundToken);
		verify(userRepo, times(1)).save(any(UserEntity.class));
	}

	@Test
	void activateUser_tokenNotFound_userNotActivated() throws Exception {

		String exampleNotExistingTokenValue = "000-000-000";
		given(activationTokenRepo.findByValue(exampleNotExistingTokenValue)).willReturn(Optional.empty());

		mockMvc.perform(
						put("/api/token?value=" + exampleNotExistingTokenValue))
				.andExpect(status().isNotFound())
				.andExpect(unauthenticated());

		verify(activationTokenRepo, never()).delete(any(ActivationTokenEntity.class));
		verify(userRepo, never()).save(any(UserEntity.class));
	}

	@Test
	void activateUser_nullTokenValue_userNotActivated() throws Exception {
		
		mockMvc.perform(put("/api/token"))
				.andExpect(status().isBadRequest())
				.andExpect(unauthenticated());

		verify(activationTokenRepo, never()).delete(any(ActivationTokenEntity.class));
		verify(userRepo, never()).save(any(UserEntity.class));
	}
}
