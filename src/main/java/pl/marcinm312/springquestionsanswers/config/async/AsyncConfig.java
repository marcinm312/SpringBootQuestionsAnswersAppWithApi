package pl.marcinm312.springquestionsanswers.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableRetry
public class AsyncConfig implements AsyncConfigurer {

	@Bean(name = "mailExecutor")
	public TaskExecutor getMailExecutor() {

		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setMaxPoolSize(5);  // Maximum number of threads in the pool
		executor.setThreadNamePrefix("MailExecutor-");  // Prefix for thread names
		executor.setWaitForTasksToCompleteOnShutdown(true);  // Ensures tasks complete on shutdown
		executor.setAwaitTerminationSeconds(60);  // Timeout for waiting for tasks to complete
		executor.initialize();  // Initializes the thread pool
		return executor;
	}
}
