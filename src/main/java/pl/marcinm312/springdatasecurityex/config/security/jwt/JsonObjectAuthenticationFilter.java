package pl.marcinm312.springdatasecurityex.config.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.marcinm312.springdatasecurityex.config.security.model.LoginCredentials;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

public class JsonObjectAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final ObjectMapper objectMapper;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	public JsonObjectAuthenticationFilter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

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
		} catch (IOException e) {
			log.error("Error while authenticating user: {}", e.getClass().getName());
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			} catch (IOException ex) {
				log.error("Error while sending error: {}", ex.getMessage());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
			return null;
		}
	}
}
