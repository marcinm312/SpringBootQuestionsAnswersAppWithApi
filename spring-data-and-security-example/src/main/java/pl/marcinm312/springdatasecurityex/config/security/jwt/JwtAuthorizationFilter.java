package pl.marcinm312.springdatasecurityex.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import pl.marcinm312.springdatasecurityex.service.db.UserDetailsServiceImpl;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

	private static final String TOKEN_HEADER = "Authorization";
	private static final String TOKEN_PREFIX = "Bearer ";
	private final UserDetailsServiceImpl userDetailsService;
	private final String secret;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	public JwtAuthorizationFilter(AuthenticationManager authenticationManager,
								  UserDetailsServiceImpl userDetailsService,
								  String secret) {
		super(authenticationManager);
		this.userDetailsService = userDetailsService;
		this.secret = secret;
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
			try {
				userName = JWT.require(Algorithm.HMAC256(secret.getBytes()))
						.build()
						.verify(token.replace(TOKEN_PREFIX, ""))
						.getSubject();
			} catch (Exception exc) {
				log.error("Error while decoding JWT: {}", exc.getMessage());
			}
			if (userName != null) {
				UserDetails userDetails;
				try {
					userDetails = userDetailsService.loadUserByUsername(userName);
				} catch (UsernameNotFoundException exc) {
					log.error("User not found!");
					return null;
				}
				return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());
			} else {
				log.error("Username taken from the token is null!");
			}
		} else {
			log.error("Token not found in header!");
		}
		return null;
	}
}
