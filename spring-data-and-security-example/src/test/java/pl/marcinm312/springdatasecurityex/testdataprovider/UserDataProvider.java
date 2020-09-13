package pl.marcinm312.springdatasecurityex.testdataprovider;

import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.model.User;

public class UserDataProvider {

    public static User prepareExampleGoodAdministrator() {
        return new User(1000L, "administrator", "password", Roles.ROLE_ADMIN.name(), true, "System", "Admin", "test@abc.pl");
    }

    public static User prepareExampleGoodUser() {
        return new User(1001L, "user", "password", Roles.ROLE_USER.name(), true, "User", "Test", "test@abc.pl");
    }

    public static User prepareExampleSecondGoodUser() {
        return new User(1002L, "user2", "password", Roles.ROLE_USER.name(), true, "User2", "Test2", "test@abc.pl");
    }

    public static User prepareUserWithConfirmPasswordErrorToRequest() {
        return new User(null, "user", null, "password", "anotherPassword", null, false, "User", "Test", "test@abc.pl");
    }

    public static User prepareGoodUserToRequest() {
        return new User(null, "user", null, "password", "password", null, false, "User", "Test", "test@abc.pl");
    }
}
