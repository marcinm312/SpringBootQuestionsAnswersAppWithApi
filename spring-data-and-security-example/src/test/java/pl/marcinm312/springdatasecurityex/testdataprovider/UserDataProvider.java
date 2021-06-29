package pl.marcinm312.springdatasecurityex.testdataprovider;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserCreate;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserDataUpdate;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserPasswordUpdate;

import java.util.Calendar;

public class UserDataProvider {

	public static User prepareExampleGoodAdministratorWithEncodedPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return new User(1000L, "administrator", passwordEncoder.encode("password"),
				Roles.ROLE_ADMIN.name(), true, "admin@abc.pl",
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
	}

	public static User prepareExampleGoodUserWithEncodedPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return new User(1001L, "user", passwordEncoder.encode("password"),
				Roles.ROLE_USER.name(), true, "test@abc.pl",
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
	}

	public static User prepareExampleSecondGoodUserWithEncodedPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return new User(1002L, "user2", passwordEncoder.encode("password"),
				Roles.ROLE_USER.name(), true, "test2@abc.pl",
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
	}

	public static User prepareExampleDisabledUserWithEncodedPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return new User(1001L, "user", passwordEncoder.encode("password"),
				Roles.ROLE_USER.name(), false, "test@abc.pl",
				DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
	}

	public static UserCreate prepareGoodUserToRequest() {
		return new UserCreate("user", "password", "password", "test@abc.pl");
	}

	public static UserCreate prepareUserWithConfirmPasswordErrorToRequest() {
		return new UserCreate("user", "password", "anotherPassword", "test@abc.pl");
	}

	public static UserCreate prepareIncorrectUserToRequest() {
		return new UserCreate("aa", "passw", "passw", "email");
	}

	public static UserCreate prepareEmptyUserToRequest() {
		return new UserCreate("", "", "", "");
	}

	public static UserDataUpdate prepareGoodUserDataUpdateToRequest() {
		return new UserDataUpdate("user", "test@abc.pl");
	}

	public static UserDataUpdate prepareGoodUserDataUpdateWithLoginChangeToRequest() {
		return new UserDataUpdate("user3", "test@abc.pl");
	}

	public static UserDataUpdate prepareExistingUserDataUpdateToRequest() {
		return new UserDataUpdate("user2", "test@abc.pl");
	}

	public static UserDataUpdate prepareIncorrectUserDataUpdateToRequest() {
		return new UserDataUpdate("aa", "email");
	}

	public static UserDataUpdate prepareEmptyUserDataUpdateToRequest() {
		return new UserDataUpdate("", "");
	}

	public static UserPasswordUpdate prepareGoodUserPasswordUpdateToRequest() {
		return new UserPasswordUpdate("password", "password2", "password2");
	}

	public static UserPasswordUpdate prepareUserPasswordUpdateWithIncorrectCurrentPasswordToRequest() {
		return new UserPasswordUpdate("aaaaaaa", "password2", "password2");
	}

	public static UserPasswordUpdate prepareUserPasswordUpdateWithConfirmationErrorToRequest() {
		return new UserPasswordUpdate("password", "password2", "password3");
	}

	public static UserPasswordUpdate prepareUserPasswordUpdateWithTheSamePasswordAsPreviousToRequest() {
		return new UserPasswordUpdate("password", "password", "password");
	}

	public static UserPasswordUpdate prepareUserPasswordUpdateWithTooShortPasswordToRequest() {
		return new UserPasswordUpdate("password", "passw", "passw");
	}

	public static UserPasswordUpdate prepareEmptyUserPasswordUpdateToRequest() {
		return new UserPasswordUpdate("", "", "");
	}
}
