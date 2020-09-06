package pl.marcinm312.springdatasecurityex.testdataprovider;

import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.model.User;

public class UserDataProvider {

    public static User prepareExampleGoodUser() {
        return new User(1000L, "user", "password", Roles.ROLE_USER.name(), true, "User", "Test", "test@abc.pl");
    }

    public static User prepareUserWithConfirmPasswordErrorToRequest() {
        return new User(null, "user", null, "password", "anotherPassword", Roles.ROLE_USER.name(), false, "User", "Test", "test@abc.pl");
    }

    public static User prepareGoodUserToRequest() {
        return new User(null, "user", null, "password", "password", Roles.ROLE_USER.name(), false, "User", "Test", "test@abc.pl");
    }
}
