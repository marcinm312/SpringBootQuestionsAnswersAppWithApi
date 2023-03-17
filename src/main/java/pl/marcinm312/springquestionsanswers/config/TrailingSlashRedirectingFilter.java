package pl.marcinm312.springquestionsanswers.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrailingSlashRedirectingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (!request.getRequestURI().endsWith("/")
				&& (request.getRequestURI().contains("app") || request.getRequestURI().contains("register"))) {
			ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromRequest(request);
			builder.replacePath(String.format("%s/", builder.build().getPath()));
			response.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
			response.setHeader(HttpHeaders.LOCATION, builder.toUriString());
		} else {
			filterChain.doFilter(request, response);
		}
	}
}
