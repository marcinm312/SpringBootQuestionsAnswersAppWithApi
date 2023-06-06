package pl.marcinm312.springquestionsanswers.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import pl.marcinm312.springquestionsanswers.config.security.jwt.*;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class MultiHttpSecurityCustomConfig {

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@RequiredArgsConstructor
	@Configuration
	@Order(1)
	public static class ApiWebSecurityConfigurationAdapter {

		private final UserDetailsServiceImpl userDetailsService;
		private final ObjectMapper objectMapper;
		private final RestAuthenticationSuccessHandler successHandler;
		private final RestAuthenticationFailureHandler failureHandler;
		private final Environment environment;
		private final AuthenticationConfiguration authenticationConfiguration;

		private static final String ADMIN_ROLE = "ADMIN";

		@Bean
		public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {

			http.securityMatcher("/api/**")
					.authorizeHttpRequests(authorizeRequests -> authorizeRequests

					.requestMatchers(
							"/api/login", "/api/registration", "/api/token")
					.permitAll()

					.requestMatchers("/api/actuator/**").hasRole(ADMIN_ROLE)
					.anyRequest().authenticated())

					.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.addFilter(authenticationFilter())
					.addFilter(new JwtAuthorizationFilter(authenticationManager(authenticationConfiguration), userDetailsService, environment))
					.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
					.csrf(AbstractHttpConfigurer::disable);
			return http.build();
		}

		@Bean
		public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
			return authenticationConfiguration.getAuthenticationManager();
		}

		public JsonObjectAuthenticationFilter authenticationFilter() throws Exception {

			JsonObjectAuthenticationFilter authenticationFilter = new JsonObjectAuthenticationFilter(objectMapper);
			authenticationFilter.setFilterProcessesUrl("/api/login");
			authenticationFilter.setAuthenticationSuccessHandler(successHandler);
			authenticationFilter.setAuthenticationFailureHandler(failureHandler);
			authenticationFilter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
			return authenticationFilter;
		}
	}

	@Configuration
	@Order(2)
	public static class FormLoginWebSecurityConfigurationAdapter {

		@Bean
		public SecurityFilterChain formLoginFilterChain(HttpSecurity http) throws Exception {

			http.securityMatcher("/**")
					.authorizeHttpRequests(authorizeRequests -> authorizeRequests

					.requestMatchers(
							"/", "/register", "/register/", "/token", "/token/", "/error", "error/",
							"/favicon.ico",
							//CSS
							"/css/style.css", "/css/signin.css",
							//JS
							"/js/clearPasswordsFieldsInRegistrationForm.js",
							//SWAGGER
							"/swagger/**","/swagger-ui/**","/swagger-ui.html","/webjars/**",
							"/swagger-resources/**","/configuration/**","/v3/api-docs/**")
					.permitAll()
					.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()

					.requestMatchers(
							"/app/**",
							"/js/clearChangePasswordForm.js")
					.authenticated()
					.anyRequest().denyAll())

					.formLogin(formLogin -> formLogin.loginPage("/loginPage/").loginProcessingUrl("/authenticate/").permitAll())
					.logout(logout -> logout.permitAll().logoutSuccessUrl("/").logoutRequestMatcher(new AntPathRequestMatcher("/logout")))

					.sessionManagement(sessionManagement -> sessionManagement.maximumSessions(10000).maxSessionsPreventsLogin(false).expiredUrl("/loginPage/").sessionRegistry(sessionRegistry()));

			return http.build();
		}

		@Bean
		SessionRegistry sessionRegistry() {
			return new SessionRegistryImpl();
		}

		@Bean
		public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
			return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
		}
	}
}
