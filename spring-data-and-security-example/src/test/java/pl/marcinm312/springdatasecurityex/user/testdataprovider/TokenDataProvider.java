package pl.marcinm312.springdatasecurityex.user.testdataprovider;

import pl.marcinm312.springdatasecurityex.user.model.Token;
import pl.marcinm312.springdatasecurityex.user.model.User;

public class TokenDataProvider {

	public static Token prepareExampleToken() {
		User user = UserDataProvider.prepareExampleDisabledUserWithEncodedPassword();
		return new Token(1000L, "123456-123-123-1234", user);
	}
}
