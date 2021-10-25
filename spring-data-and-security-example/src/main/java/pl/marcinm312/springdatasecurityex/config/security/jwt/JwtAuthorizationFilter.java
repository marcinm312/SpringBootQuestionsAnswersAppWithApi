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
import java.util.Optional;

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
		if (token != null && token.startsWith(TOKEN_PREFIX)) {
			String userName = null;
			Date issuedAt = null;
			try {
				String secret = environment.getProperty("jwt.secret");
				if (secret != null) {
					DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret.getBytes()))
							.build()
							.verify(token.replace(TOKEN_PREFIX, ""));
					userName = decodedJWT.getSubject();
					issuedAt = decodedJWT.getIssuedAt();
				}
			} catch (Exception exc) {
				log.error("Error while decoding JWT: {}", exc.getMessage());
			}
			if (userName != null && issuedAt != null) {
				Optional<User> optionalUser = userDetailsService.findUserByUsername(userName);
				if (optionalUser.isPresent()) {
					User userFromDB = optionalUser.get();
					return new UsernamePasswordAuthenticationToken(userFromDB.getUsername(), null, userFromDB.getAuthorities());
				} else {
					log.error("User not found!");
				}
			} else {
				log.error("Username taken from the token is null!");
			}
		} else {
			log.error("Token not found in header!");
		}
		return null;
	}
}
