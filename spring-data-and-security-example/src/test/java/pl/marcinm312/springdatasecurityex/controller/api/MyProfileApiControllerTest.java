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
import pl.marcinm312.springdatasecurityex.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserGet;
import pl.marcinm312.springdatasecurityex.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.MailService;
import pl.marcinm312.springdatasecurityex.service.db.UserDetailsServiceImpl;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;
import pl.marcinm312.springdatasecurityex.utils.SessionUtils;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MyProfileApiController.class)
@ComponentScan(basePackageClasses = MyProfileApiController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = MyProfileApiController.class)
		})
@MockBeans({@MockBean(TokenRepo.class), @MockBean(MailService.class), @MockBean(SessionUtils.class)})
@SpyBeans({@SpyBean(UserManager.class), @SpyBean(UserDetailsServiceImpl.class)})
@Import({MultiHttpSecurityCustomConfig.class})
class MyProfileApiControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private UserRepo userRepo;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper mapper = new ObjectMapper();

	@BeforeEach
	void setup() {
		User commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		given(userRepo.findByUsername("user")).willReturn(Optional.of(commonUser));

		User adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();
		given(userRepo.findByUsername("administrator")).willReturn(Optional.of(adminUser));

		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(this.webApplicationContext)
						.apply(springSecurity())
						.alwaysDo(print())
						.build();
	}

	@Test
	@WithMockUser(username = "user")
	void getMyProfile_loggedCommonUser_success() throws Exception {
		User expectedUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		String response = mockMvc.perform(
				get("/api/myProfile")
						.with(httpBasic("user", "password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(authenticated().withUsername("user").withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		UserGet responseUser = mapper.readValue(response, UserGet.class);

		assertUser(expectedUser, responseUser);
	}

	@Test
	@WithMockUser(username = "administrator", roles = {"ADMIN"})
	void getMyProfile_loggedAdminUser_success() throws Exception {
		User expectedUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();
		String response = mockMvc.perform(
						get("/api/myProfile")
								.with(httpBasic("administrator", "password")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(authenticated().withUsername("administrator").withRoles("ADMIN"))
				.andReturn().getResponse().getContentAsString();

		UserGet responseUser = mapper.readValue(response, UserGet.class);

		assertUser(expectedUser, responseUser);
	}

	@Test
	@WithAnonymousUser
	void getMyProfile_withAnonymousUser_unauthorized() throws Exception {
		mockMvc.perform(
				get("/api/myProfile"))
				.andExpect(status().isUnauthorized())
				.andExpect(unauthenticated());
	}

	private void assertUser(User expectedUser, UserGet responseUser) {
		Assertions.assertEquals(expectedUser.getId(), responseUser.getId());
		Assertions.assertEquals(expectedUser.getCreatedAt(), responseUser.getCreatedAt());
		Assertions.assertEquals(expectedUser.getUpdatedAt(), responseUser.getUpdatedAt());
		Assertions.assertEquals(expectedUser.getUsername(), responseUser.getUsername());
		Assertions.assertEquals(expectedUser.getRole(), responseUser.getRole());
		Assertions.assertEquals(expectedUser.getEmail(), responseUser.getEmail());
		Assertions.assertEquals(expectedUser.isEnabled(), responseUser.isEnabled());
	}
}