package pl.marcinm312.springdatasecurityex.user.testdataprovider;

import pl.marcinm312.springdatasecurityex.user.model.TokenEntity;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

public class TokenDataProvider {

	public static TokenEntity prepareExampleToken() {
		UserEntity user = UserDataProvider.prepareExampleDisabledUserWithEncodedPassword();
		return buildTokenEntity(1000L, "123456-123-123-1234", user);
	}

	private static TokenEntity buildTokenEntity(Long id, String tokenValue, UserEntity user) {
		return TokenEntity.builder()
				.id(id)
				.value(tokenValue)
				.user(user)
				.build();
	}
}
