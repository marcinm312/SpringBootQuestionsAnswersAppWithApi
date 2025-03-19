package pl.marcinm312.springquestionsanswers.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springquestionsanswers.config.async.AsyncConfig;
import pl.marcinm312.springquestionsanswers.config.security.MultiHttpSecurityCustomConfig;
import pl.marcinm312.springquestionsanswers.config.security.SecurityMessagesConfig;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationFailureHandler;
import pl.marcinm312.springquestionsanswers.config.security.jwt.RestAuthenticationSuccessHandler;
import pl.marcinm312.springquestionsanswers.shared.testdataprovider.JwtProvider;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;
import pl.marcinm312.springquestionsanswers.user.service.UserAdminManager;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;
import pl.marcinm312.springquestionsanswers.user.testdataprovider.UserDataProvider;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserAdminApiController.class)
@ComponentScan(basePackageClasses = UserAdminApiController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = UserAdminApiController.class)
		})
@SpyBeans({@SpyBean(UserDetailsServiceImpl.class), @SpyBean(RestAuthenticationSuccessHandler.class),
		@SpyBean(RestAuthenticationFailureHandler.class)})
@Import({MultiHttpSecurityCustomConfig.class, SecurityMessagesConfig.class, AsyncConfig.class})
class UserAdminApiControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private UserRepo userRepo;

	@SpyBean
	private UserAdminManager userAdminManager;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

	private final UserEntity commonUser = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
	private final UserEntity adminUser = UserDataProvider.prepareExampleGoodAdministratorWithEncodedPassword();

	@BeforeEach
	void setUp() {

		given(userRepo.getNonEnabledOldUsers(any(LocalDate.class)))
				.willReturn(UserDataProvider.prepareExampleDisabledUsersList());

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
	void getNonEnabledOldUsers_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(get("/api/admin/users/getNonEnabledOldUsers"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getNonEnabledOldUsers_withCommonUser_forbidden() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(get("/api/admin/users/getNonEnabledOldUsers")
						.header("Authorization", token))
				.andExpect(status().isForbidden());
	}

	@Test
	void getNonEnabledOldUsers_simpleCase_success() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("admin", "password");
		String response = mockMvc.perform(get("/api/admin/users/getNonEnabledOldUsers")
						.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		JsonNode root = mapper.readTree(response);
		int amountOfElements = root.size();
		Assertions.assertEquals(3, amountOfElements);
	}

	@Test
	void deleteNonEnabledOldUsers_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(get("/api/admin/users/deleteNonEnabledOldUsers"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deleteNonEnabledOldUsers_withCommonUser_forbidden() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("user", "password");
		mockMvc.perform(get("/api/admin/users/deleteNonEnabledOldUsers")
						.header("Authorization", token))
				.andExpect(status().isForbidden());
	}

	@Test
	void deleteNonEnabledOldUsers_simpleCase_success() throws Exception {

		String token = new JwtProvider(mockMvc).prepareToken("admin", "password");
		mockMvc.perform(delete("/api/admin/users/deleteNonEnabledOldUsers")
						.header("Authorization", token))
				.andExpect(status().isOk());
	}

	@Test
	void deleteNonEnabledOldUsers_startingFromCron_runAtLeastOnce() {

		verify(userAdminManager, timeout(4000).atLeastOnce()).deleteNonEnabledOldUsers();
	}
}