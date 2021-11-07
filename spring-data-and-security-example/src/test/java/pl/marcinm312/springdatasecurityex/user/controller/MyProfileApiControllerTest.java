package pl.marcinm312.springdatasecurityex.user.controller;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springdatasecurityex.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.config.security.SecurityMessagesConfig;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springdatasecurityex.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserGet;
import pl.marcinm312.springdatasecurityex.user.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.user.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.shared.mail.MailService;
import pl.marcinm312.springdatasecurityex.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.config.security.utils.SessionUtils;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MyProfileApiController.class)
@ComponentScan(basePackageClasses = MyProfileApiController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = MyProfileApiController.class)
		})
@MockBeans({@MockBean(TokenRepo.class), @MockBean(MailService.class), @MockBean(SessionUtils.class)})
@SpyBeans({@SpyBean(UserManager.class), @SpyBean(UserDetailsServiceImpl.class),
		@SpyBean(RestAuthenticationSuccessHandler.class), @SpyBean(RestAuthenticationFailureHandler.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class})
class MyProfileApiControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private UserRepo userRepo;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper mapper = new ObjectMapper();

	private final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();

	@BeforeEach
	void setup() {
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
	void getMyProfile_loggedCommonUser_success() throws Exception {

		String token = prepareToken("user", "password");

		UserEntity expectedUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		String response = mockMvc.perform(
						get("/api/myProfile")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		UserGet responseUser = mapper.readValue(response, UserGet.class);

		assertUser(expectedUser, responseUser);
	}

	@Test
	void getMyProfile_loggedAdminUser_success() throws Exception {

		String token = prepareToken("administrator", "password");

		UserEntity expectedUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();
		String response = mockMvc.perform(
						get("/api/myProfile")
								.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		UserGet responseUser = mapper.readValue(response, UserGet.class);

		assertUser(expectedUser, responseUser);
	}

	@Test
	void getMyProfile_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
						get("/api/myProfile"))
				.andExpect(status().isUnauthorized());
	}

	private void assertUser(UserEntity expectedUser, UserGet responseUser) {
		Assertions.assertEquals(expectedUser.getId(), responseUser.getId());
		Assertions.assertEquals(expectedUser.getCreatedAt(), responseUser.getCreatedAt());
		Assertions.assertEquals(expectedUser.getUpdatedAt(), responseUser.getUpdatedAt());
		Assertions.assertEquals(expectedUser.getUsername(), responseUser.getUsername());
		Assertions.assertEquals(expectedUser.getRole(), responseUser.getRole());
		Assertions.assertEquals(expectedUser.getEmail(), responseUser.getEmail());
		Assertions.assertEquals(expectedUser.isEnabled(), responseUser.isEnabled());
	}

	private String prepareToken(String username, String password) throws Exception {
		return mockMvc.perform(post("/api/login")
						.content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
						.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(header().exists("Authorization"))
				.andReturn().getResponse().getHeader("Authorization");
	}
}
