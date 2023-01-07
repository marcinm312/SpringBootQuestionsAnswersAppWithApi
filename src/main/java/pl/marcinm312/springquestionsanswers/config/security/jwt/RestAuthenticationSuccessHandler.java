package pl.marcinm312.springquestionsanswers.config.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Component
public class RestAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final Environment environment;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										Authentication authentication) {

		String secret = environment.getProperty("jwt.secret");
		String expirationTimeString = environment.getProperty("jwt.expirationTime");
		if (secret != null && expirationTimeString != null) {
			long expirationTime = Long.parseLong(expirationTimeString) * 60000;
			UserEntity principal = (UserEntity) authentication.getPrincipal();
			String token = JwtCreator.createJWT(principal.getId().toString(), expirationTime, secret.getBytes());
			response.addHeader("Authorization", "Bearer " + token);
		}
	}
}
