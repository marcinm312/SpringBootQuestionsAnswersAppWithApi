package pl.marcinm312.springdatasecurityex.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.service.db.UserDetailsServiceImpl;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

	private static final String TOKEN_HEADER = "Authorization";
	private static final String TOKEN_PREFIX = "Bearer ";
	private final UserDetailsServiceImpl userDetailsService;
	private final Environment environment;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

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
			Date issuedAt = null;
			try {
				DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret.getBytes()))
						.build()
						.verify(token.replace(TOKEN_PREFIX, ""));
				userId = decodedJWT.getSubject();
				issuedAt = decodedJWT.getIssuedAt();
			} catch (Exception exc) {
				log.error("Error while decoding JWT: {}", exc.getMessage());
			}
			if (userId != null && issuedAt != null) {
				return getAndVerifyUserAndReturnAuthenticationToken(userId, issuedAt);
			} else {
				log.error("Username or creation date taken from the token is null!");
			}
		} else {
			log.error("Token not found in header!");
		}
		return null;
	}

	private UsernamePasswordAuthenticationToken getAndVerifyUserAndReturnAuthenticationToken(String userId, Date issuedAt) {
		User user;
		try {
			user = userDetailsService.findUserById(Long.valueOf(userId));
		} catch (Exception exc) {
			log.error("Error while searching user: {} {}", exc.getClass().getName(), exc.getMessage());
			return null;
		}
		if (issuedAt.after(user.getDateToCompareInJwt())) {
			return new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities());
		} else {
			log.error("The token has expired due to logging out of the user or changing the password");
			return null;
		}
	}
}
