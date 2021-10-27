package pl.marcinm312.springdatasecurityex.config.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pl.marcinm312.springdatasecurityex.model.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RestAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final Environment environment;

	@Autowired
	public RestAuthenticationSuccessHandler(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										Authentication authentication) {

		String secret = environment.getProperty("jwt.secret");
		String expirationTimeString = environment.getProperty("jwt.expirationTime");
		if (secret != null && expirationTimeString != null) {
			long expirationTime = Long.parseLong(expirationTimeString) * 60000;
			User principal = (User) authentication.getPrincipal();
			String token = JwtCreator.createJWT(principal.getId().toString(), expirationTime, secret.getBytes());
			response.addHeader("Authorization", "Bearer " + token);
		}
	}
}
