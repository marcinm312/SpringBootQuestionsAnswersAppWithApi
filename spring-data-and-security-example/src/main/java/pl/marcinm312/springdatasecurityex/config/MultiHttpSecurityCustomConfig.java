package pl.marcinm312.springdatasecurityex.config;

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

import pl.marcinm312.springdatasecurityex.service.db.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class MultiHttpSecurityCustomConfig {

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Configuration
	@Order(1)
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

		private UserDetailsServiceImpl userDetailsService;

		@Autowired
		public ApiWebSecurityConfigurationAdapter(UserDetailsServiceImpl userDetailsService) {
			this.userDetailsService = userDetailsService;
		}

		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(userDetailsService);
		}

		protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/api/**").authorizeRequests().anyRequest().authenticated().and().httpBasic().and()
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().csrf().disable();
		}
	}

	@Configuration
	@Order(2)
	public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

		private UserDetailsServiceImpl userDetailsService;

		@Autowired
		public FormLoginWebSecurityConfigurerAdapter(UserDetailsServiceImpl userDetailsService) {
			this.userDetailsService = userDetailsService;
		}

		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(userDetailsService);
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/**").authorizeRequests().antMatchers("/", "/register", "/register/", "/token", "/token/")
					.permitAll().anyRequest().authenticated().and().formLogin().permitAll().and().logout().permitAll()
					.logoutSuccessUrl("/").and().sessionManagement().maximumSessions(10000)
					.maxSessionsPreventsLogin(false).expiredUrl("/login").sessionRegistry(sessionRegistry());
		}

		@Bean
		SessionRegistry sessionRegistry() {
			return new SessionRegistryImpl();
		}

		@Bean
		public static ServletListenerRegistrationBean httpSessionEventPublisher() {
			return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
		}

	}
}