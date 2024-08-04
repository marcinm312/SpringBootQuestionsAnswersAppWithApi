package pl.marcinm312.springquestionsanswers.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataConfig {

	@Value("${data.rows.limit}")
	private String rowsLimit;

	@Autowired
	public void loadProperties() {
		System.setProperty("data.rows.limit", rowsLimit);
	}
}
