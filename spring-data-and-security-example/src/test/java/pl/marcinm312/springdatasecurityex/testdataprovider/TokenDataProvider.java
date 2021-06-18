package pl.marcinm312.springdatasecurityex.testdataprovider;

import pl.marcinm312.springdatasecurityex.model.user.Token;
import pl.marcinm312.springdatasecurityex.model.user.User;

public class TokenDataProvider {

	public static Token prepareExampleToken() {
		User user = UserDataProvider.prepareExampleDisabledUserWithEncodedPassword();
		return new Token(1000L, "123456-123-123-1234", user);
	}
}
