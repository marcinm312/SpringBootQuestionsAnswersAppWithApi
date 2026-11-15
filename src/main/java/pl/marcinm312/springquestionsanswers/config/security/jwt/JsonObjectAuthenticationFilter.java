package pl.marcinm312.springquestionsanswers.config.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.marcinm312.springquestionsanswers.config.security.model.LoginCredentials;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JsonObjectAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final ObjectMapper objectMapper;

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {

		try {
			BufferedReader reader = request.getReader();
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			LoginCredentials authRequest = objectMapper.readValue(sb.toString(), LoginCredentials.class);
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
					authRequest.getUsername(), authRequest.getPassword()
			);
			setDetails(request, token);
			return this.getAuthenticationManager().authenticate(token);
		} catch (IOException | JacksonException e) {
			log.error("Error while authenticating user: {}", e.getClass().getName());
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			} catch (IOException ex) {
				String errorMessage = String.format("Error while sending error: %s", ex.getMessage());
				log.error(errorMessage, ex);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
			return null;
		}
	}
}
