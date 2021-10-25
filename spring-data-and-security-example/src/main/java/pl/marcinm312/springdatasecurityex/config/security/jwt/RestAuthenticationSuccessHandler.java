package pl.marcinm312.springdatasecurityex.config.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RestAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final long expirationTime;
	private final String secret;

	@Autowired
	public RestAuthenticationSuccessHandler(
			@Value("${jwt.expirationTime}") long expirationTime,
			@Value("${jwt.secret}") String secret) {
		this.expirationTime = expirationTime * 60000;
		this.secret = secret;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										Authentication authentication) {

		UserDetails principal = (UserDetails) authentication.getPrincipal();
		String token = JwtCreator.createJWT(principal.getUsername(), expirationTime, secret.getBytes());
		response.addHeader("Authorization", "Bearer " + token);
	}
}
