package pl.marcinm312.springdatasecurityex.testdataprovider;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserCreate;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserDataUpdate;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserPasswordUpdate;

import java.util.Calendar;
import java.util.Date;

public class UserDataProvider {

	public static User prepareExampleGoodAdministratorWithEncodedPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		User user = new User();
		user.setId(1000L);
		user.setUsername("administrator");
		user.setPassword(passwordEncoder.encode("password"));
		user.setRole(Roles.ROLE_ADMIN.name());
		user.setEnabled(true);
		user.setEmail("admin@abc.pl");
		user.setCreatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
		user.setUpdatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		user.setChangePasswordDate(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		user.setTimeOfSessionExpiration(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		return user;
	}

	public static User prepareExampleGoodUserWithEncodedPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		User user = new User();
		user.setId(1001L);
		user.setUsername("user");
		user.setPassword(passwordEncoder.encode("password"));
		user.setRole(Roles.ROLE_USER.name());
		user.setEnabled(true);
		user.setEmail("test@abc.pl");
		user.setCreatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
		user.setUpdatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		user.setChangePasswordDate(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		user.setTimeOfSessionExpiration(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		return user;
	}

	public static User prepareExampleSecondGoodUserWithEncodedPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		User user = new User();
		user.setId(1002L);
		user.setUsername("user2");
		user.setPassword(passwordEncoder.encode("password"));
		user.setRole(Roles.ROLE_USER.name());
		user.setEnabled(true);
		user.setEmail("test2@abc.pl");
		user.setCreatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
		user.setUpdatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		user.setChangePasswordDate(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		user.setTimeOfSessionExpiration(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		return user;
	}

	public static User prepareExampleDisabledUserWithEncodedPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		User user = new User();
		user.setId(1001L);
		user.setUsername("user");
		user.setPassword(passwordEncoder.encode("password"));
		user.setRole(Roles.ROLE_USER.name());
		user.setEnabled(false);
		user.setEmail("test@abc.pl");
		user.setCreatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
		user.setUpdatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		user.setChangePasswordDate(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		user.setTimeOfSessionExpiration(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		return user;
	}

	public static User prepareExampleSecondDisabledUserWithEncodedPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		User user = new User();
		user.setId(1003L);
		user.setUsername("user3");
		user.setPassword(passwordEncoder.encode("password"));
		user.setRole(Roles.ROLE_USER.name());
		user.setEnabled(false);
		user.setEmail("test@abc.pl");
		user.setCreatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
		user.setUpdatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		user.setChangePasswordDate(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		user.setTimeOfSessionExpiration(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 30, 30));
		return user;
	}

	public static User prepareExampleGoodUserWithEncodedPasswordWithSpaces() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		User user = new User();
		user.setId(1003L);
		user.setUsername("user3");
		user.setPassword(passwordEncoder.encode(" pass "));
		user.setRole(Roles.ROLE_USER.name());
		user.setEnabled(true);
		user.setEmail("test3@abc.pl");
		user.setCreatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 15, 10, 25, 30));
		user.setUpdatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 15, 10, 30, 30));
		user.setChangePasswordDate(DateProvider.prepareDate(2020, Calendar.JANUARY, 15, 10, 30, 30));
		user.setTimeOfSessionExpiration(DateProvider.prepareDate(2020, Calendar.JANUARY, 15, 10, 30, 30));
		return user;
	}

	public static User prepareExampleGoodUserWithEncodedAndChangedPassword() {
		long currentTime = System.currentTimeMillis();
		Date futureDate = new Date(currentTime + 10 * 60000);
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		User user = new User();
		user.setId(1004L);
		user.setUsername("user4");
		user.setPassword(passwordEncoder.encode("password"));
		user.setRole(Roles.ROLE_USER.name());
		user.setEnabled(true);
		user.setEmail("test4@abc.pl");
		user.setCreatedAt(DateProvider.prepareDate(2020, Calendar.JANUARY, 10, 10, 25, 30));
		user.setUpdatedAt(futureDate);
		user.setChangePasswordDate(futureDate);
		user.setTimeOfSessionExpiration(futureDate);
		return user;
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

	public static UserCreate prepareUserWithTooShortLoginAfterTrimToRequest() {
		return new UserCreate(" a ", "password", "password", "test@abc.pl");
	}

	public static UserCreate prepareUserWithSpacesInPasswordToRequest() {
		return new UserCreate("user", " pass ", " pass ", "test@abc.pl");
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

	public static UserDataUpdate prepareUserDataUpdateWithTooShortLoginAfterTrimToRequest() {
		return new UserDataUpdate(" a ", "test@abc.pl");
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

	public static UserPasswordUpdate prepareUserPasswordUpdateWithSpacesInPassToRequest() {
		return new UserPasswordUpdate(" pass ", " pas  ", " pas  ");
	}
}
