package pl.marcinm312.springdatasecurityex.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.testdataprovider.UserDataProvider;

class PermissionsUtilsTest {

	@Test
	void checkIfUserIsPermitted_unexpectedObjectType_false() {
		User user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		boolean isUserPermitted = PermissionsUtils.checkIfUserIsPermitted(user, user);
		Assertions.assertFalse(isUserPermitted);
	}
}
