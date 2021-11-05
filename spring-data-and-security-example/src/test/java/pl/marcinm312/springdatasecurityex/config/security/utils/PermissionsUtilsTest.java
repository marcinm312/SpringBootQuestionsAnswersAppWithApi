package pl.marcinm312.springdatasecurityex.config.security.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.testdataprovider.UserDataProvider;

class PermissionsUtilsTest {

	@Test
	void checkIfUserIsPermitted_unexpectedObjectType_false() {
		UserEntity user = UserDataProvider.prepareExampleGoodUserWithEncodedPassword();
		boolean isUserPermitted = PermissionsUtils.checkIfUserIsPermitted(user, user);
		Assertions.assertFalse(isUserPermitted);
	}
}
