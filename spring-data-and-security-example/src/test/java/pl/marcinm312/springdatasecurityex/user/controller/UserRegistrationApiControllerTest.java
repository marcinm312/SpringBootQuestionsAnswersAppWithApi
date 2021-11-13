package pl.marcinm312.springdatasecurityex.user.controller;

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
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springdatasecurityex.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.config.security.SecurityMessagesConfig;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springdatasecurityex.config.security.utils.SessionUtils;
import pl.marcinm312.springdatasecurityex.shared.mail.MailService;
import pl.marcinm312.springdatasecurityex.user.model.TokenEntity;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserCreate;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserGet;
import pl.marcinm312.springdatasecurityex.user.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.user.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.user.validator.UserCreateValidator;

import javax.mail.MessagingException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserRegistrationApiController.class)
@ComponentScan(basePackageClasses = UserRegistrationApiController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UserRegistrationApiController.class)
		})
@SpyBeans({@SpyBean(UserManager.class), @SpyBean(UserCreateValidator.class), @SpyBean(SessionUtils.class),
		@SpyBean(UserDetailsServiceImpl.class), @SpyBean(PasswordEncoder.class),
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
@WebAppConfiguration
class UserRegistrationApiControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private MailService mailService;

	@MockBean
	private UserRepo userRepo;

	@MockBean
	private TokenRepo tokenRepo;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper mapper = new ObjectMapper();

	@BeforeEach
	void setup() throws MessagingException {
		doNothing().when(mailService).sendMail(isA(String.class), isA(String.class), isA(String.class), isA(boolean.class));
		doNothing().when(tokenRepo).delete(isA(TokenEntity.class));

		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.webApplicationContext)
				.apply(springSecurity())
				.alwaysDo(print())
				.build();
	}

	@Test
	void createUser_simpleCase_success() throws Exception {
		UserCreate userToRequest = UserDataProvider.prepareGoodUserToRequest();
		UserEntity user = new UserEntity(userToRequest.getUsername(), userToRequest.getPassword(), userToRequest.getEmail());
		given(userRepo.findByUsername(userToRequest.getUsername())).willReturn(Optional.empty());
		given(tokenRepo.save(any(TokenEntity.class))).willReturn(new TokenEntity(null, "123456789", user));
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
		verify(mailService, times(1)).sendMail(any(String.class), any(String.class),
				any(String.class), eq(true));
	}
}