package pl.marcinm312.springdatasecurityex.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import pl.marcinm312.springdatasecurityex.service.db.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class MultiHttpSecurityCustomConfig extends WebSecurityConfigurerAdapter {

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Configuration
	@Order(1)
	public class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

		private final UserDetailsServiceImpl userDetailsService;

		@Autowired
		public ApiWebSecurityConfigurationAdapter(UserDetailsServiceImpl userDetailsService) {
			this.userDetailsService = userDetailsService;
		}

		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(userDetailsService).passwordEncoder(getPasswordEncoder());
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/api/**")
					.authorizeRequests().anyRequest().authenticated()
					.and().httpBasic()
					.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
					.and().csrf().disable();
		}
	}

	@Configuration
	@Order(2)
	public class FormLoginWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

		private final UserDetailsServiceImpl userDetailsService;

		@Autowired
		public FormLoginWebSecurityConfigurationAdapter(UserDetailsServiceImpl userDetailsService) {
			this.userDetailsService = userDetailsService;
		}

		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(userDetailsService).passwordEncoder(getPasswordEncoder());
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/**")
					.authorizeRequests().antMatchers(
							"/", "/register", "/register/", "/token", "/token/", "/error", "error/",
							"/css/style.css", "/css/signin.css", "/favicon.ico",
							"/js/clearPasswordsFieldsInRegistrationForm.js")
					.permitAll()
					.anyRequest().authenticated()
					.and().formLogin().loginPage("/loginPage").loginProcessingUrl("/authenticate").permitAll()
					.and().logout().permitAll().logoutSuccessUrl("/").logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
					.and().sessionManagement().maximumSessions(10000).maxSessionsPreventsLogin(false).expiredUrl("/loginPage").sessionRegistry(sessionRegistry());
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