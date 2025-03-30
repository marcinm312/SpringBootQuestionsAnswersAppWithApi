package pl.marcinm312.springquestionsanswers.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
@EnableAsync
@EnableRetry
public class AsyncConfig implements AsyncConfigurer {

	@Bean(name = "mailExecutor")
	public Executor getMailExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}
}
