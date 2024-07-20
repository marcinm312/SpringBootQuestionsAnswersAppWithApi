package pl.marcinm312.springquestionsanswers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

	@Override
	@Bean
	public TaskExecutor getAsyncExecutor() {

		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setMaxPoolSize(5);  // Maximum number of threads in the pool
		executor.setWaitForTasksToCompleteOnShutdown(true);  // Ensures tasks complete on shutdown
		executor.setAwaitTerminationSeconds(60);  // Timeout for waiting for tasks to complete
		executor.initialize();  // Initializes the thread pool
		return executor;
	}
}
