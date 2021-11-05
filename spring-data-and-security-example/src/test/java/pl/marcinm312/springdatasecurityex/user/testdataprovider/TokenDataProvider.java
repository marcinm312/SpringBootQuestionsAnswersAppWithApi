package pl.marcinm312.springdatasecurityex.user.testdataprovider;

import pl.marcinm312.springdatasecurityex.user.model.TokenEntity;
import pl.marcinm312.springdatasecurityex.user.model.User;

public class TokenDataProvider {

	public static TokenEntity prepareExampleToken() {
		User user = UserDataProvider.prepareExampleDisabledUserWithEncodedPassword();
		return new TokenEntity(1000L, "123456-123-123-1234", user);
	}
}
