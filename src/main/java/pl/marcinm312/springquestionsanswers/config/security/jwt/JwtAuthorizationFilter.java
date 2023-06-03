package pl.marcinm312.springquestionsanswers.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

	private static final String TOKEN_HEADER = "Authorization";
	private static final String TOKEN_PREFIX = "Bearer ";
	private final UserDetailsServiceImpl userDetailsService;
	private final Environment environment;


	public JwtAuthorizationFilter(AuthenticationManager authenticationManager,
								  UserDetailsServiceImpl userDetailsService,
								  Environment environment) {

		super(authenticationManager);
		this.userDetailsService = userDetailsService;
		this.environment = environment;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
									FilterChain filterChain) throws IOException, ServletException {

		UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
		if (authentication == null) {
			filterChain.doFilter(request, response);
			return;
		}
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {

		String token = request.getHeader(TOKEN_HEADER);
		String secret = environment.getProperty("jwt.secret");
		if (secret != null && token != null && token.startsWith(TOKEN_PREFIX)) {
			String userId = null;
			Instant issuedAt = null;
			try {
				DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret.getBytes()))
						.build()
						.verify(token.replace(TOKEN_PREFIX, ""));
				userId = decodedJWT.getSubject();
				issuedAt = decodedJWT.getIssuedAtAsInstant();
			} catch (Exception exc) {
				log.error("Error while decoding JWT: {}", exc.getMessage());
			}
			if (userId != null && issuedAt != null) {
				return getAndVerifyUserAndReturnAuthenticationToken(userId, issuedAt);
			}
			log.error("Username or creation date taken from the token is null!");
		}
		return null;
	}

	private UsernamePasswordAuthenticationToken getAndVerifyUserAndReturnAuthenticationToken(String userId, Instant issuedAt) {

		UserEntity user;
		try {
			user = userDetailsService.findUserById(Long.valueOf(userId));
		} catch (Exception exc) {
			String errorMessage = String.format("Error while searching user: %s %s", exc.getClass().getName(), exc.getMessage());
			log.error(errorMessage, exc);
			return null;
		}
		if (issuedAt.isAfter(user.getDateToCompareInJwt().atZone(ZoneId.systemDefault()).toInstant())) {
			return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		}
		log.error("The token has expired due to logging out of the user or changing the password");
		return null;
	}
}
