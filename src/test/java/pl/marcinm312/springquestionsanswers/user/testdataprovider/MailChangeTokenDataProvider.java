package pl.marcinm312.springquestionsanswers.user.testdataprovider;

import pl.marcinm312.springquestionsanswers.user.model.MailChangeTokenEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;

public class MailChangeTokenDataProvider {

	public static MailChangeTokenEntity prepareExampleToken() {
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		return buildTokenEntity(1000L, "123456-123-123-1234", "changed@abc.pl", user);
	}

	private static MailChangeTokenEntity buildTokenEntity(Long id, String tokenValue, String newEmail, UserEntity user) {
		return MailChangeTokenEntity.builder()
				.id(id)
				.value(tokenValue)
				.newEmail(newEmail)
				.user(user)
				.build();
	}
}
