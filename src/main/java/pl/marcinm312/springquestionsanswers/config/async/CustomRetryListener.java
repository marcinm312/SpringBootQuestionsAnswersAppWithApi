package pl.marcinm312.springquestionsanswers.config.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomRetryListener implements RetryListener {

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
												 Throwable throwable) {

		log.error("Error while executing method: {}. Exception: {}. Message: {}", callback.getLabel(),
				throwable.getCause().getClass().getName(), throwable.getMessage());
		RetryListener.super.onError(context, callback, throwable);
	}
}
