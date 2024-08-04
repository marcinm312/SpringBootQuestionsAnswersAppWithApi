package pl.marcinm312.springquestionsanswers.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class PropertiesConfig {

	private final Environment environment;

	@Autowired
	public void loadProperties() {
		System.setProperty("data.rows.limit", environment.getProperty("data.rows.limit"));
	}
}
