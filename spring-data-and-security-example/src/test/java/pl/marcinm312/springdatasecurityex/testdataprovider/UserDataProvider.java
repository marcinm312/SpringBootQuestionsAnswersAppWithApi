package pl.marcinm312.springdatasecurityex.testdataprovider;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserCreate;

import java.util.Calendar;

public class UserDataProvider {

	public static User prepareExampleGoodAdministrator() {
		return new User(1000L, "administrator", "password", Roles.ROLE_ADMIN.name(), true, "test@abc.pl",
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
	}

	public static User prepareExampleGoodAdministratorWithEncodedPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return new User(1000L, "administrator", passwordEncoder.encode("password"), Roles.ROLE_ADMIN.name(), true, "test@abc.pl",
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
	}

	public static User prepareExampleGoodUser() {
		return new User(1001L, "user", "password", Roles.ROLE_USER.name(), true, "test@abc.pl",
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
	}

	public static User prepareExampleGoodUserWithEncodedPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return new User(1001L, "user", passwordEncoder.encode("password"), Roles.ROLE_USER.name(), true, "test@abc.pl",
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
	}

	public static User prepareExampleSecondGoodUser() {
		return new User(1002L, "user2", "password", Roles.ROLE_USER.name(), true, "test@abc.pl",
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
	}

	public static UserCreate prepareUserWithConfirmPasswordErrorToRequest() {
		return new UserCreate("user", "password", "anotherPassword", "test@abc.pl");
	}

	public static UserCreate prepareGoodUserToRequest() {
		return new UserCreate("user", "password", "password", "test@abc.pl");
	}
}
