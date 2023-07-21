package pl.marcinm312.springquestionsanswers.user.testdataprovider;

import pl.marcinm312.springquestionsanswers.user.model.ActivationTokenEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;

public class ActivationTokenDataProvider {

	public static ActivationTokenEntity prepareExampleToken() {
		UserEntity user = UserDataProvider.prepareExampleDisabledUserWithEncodedPassword();
		return buildTokenEntity(1000L, "123456-123-123-1234", user);
	}

	private static ActivationTokenEntity buildTokenEntity(Long id, String tokenValue, UserEntity user) {
		return ActivationTokenEntity.builder()
				.id(id)
				.value(tokenValue)
				.user(user)
				.build();
	}
}
