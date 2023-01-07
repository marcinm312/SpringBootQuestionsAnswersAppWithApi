package pl.marcinm312.springquestionsanswers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class SpringBootQuestionsAnswersApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SpringBootQuestionsAnswersApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBootQuestionsAnswersApplication.class, args);
	}

}
