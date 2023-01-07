package pl.marcinm312.springquestionsanswers.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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

		@Bean
		public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {

			http.antMatcher("/api/**")
					.authorizeRequests().antMatchers(
							"/api/login", "/api/registration", "/api/token"
					).permitAll()
					.anyRequest().authenticated()
					.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
					.and().addFilter(authenticationFilter())
					.addFilter(new JwtAuthorizationFilter(authenticationManager(authenticationConfiguration), userDetailsService, environment))
					.exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
					.and().csrf().disable();
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

			http.antMatcher("/**")
					.authorizeRequests().antMatchers(
							"/", "/register", "/register/", "/token", "/token/", "/error", "error/",
							"/css/style.css", "/css/signin.css", "/favicon.ico",
							"/js/clearPasswordsFieldsInRegistrationForm.js")
					.permitAll()

					.antMatchers("/swagger-ui.html").permitAll()
					.antMatchers("/swagger-ui/**").permitAll()
					.antMatchers("/v2/api-docs").permitAll()
					.antMatchers("/webjars/**").permitAll()
					.antMatchers("/swagger-resources/**").permitAll()

					.anyRequest().authenticated()
					.and().formLogin().loginPage("/loginPage").loginProcessingUrl("/authenticate").permitAll()
					.and().logout().permitAll().logoutSuccessUrl("/").logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
					.and().sessionManagement().maximumSessions(10000).maxSessionsPreventsLogin(false).expiredUrl("/loginPage").sessionRegistry(sessionRegistry());
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
