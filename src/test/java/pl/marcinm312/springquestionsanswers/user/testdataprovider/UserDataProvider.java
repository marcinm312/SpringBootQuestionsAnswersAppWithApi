package pl.marcinm312.springquestionsanswers.user.testdataprovider;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.marcinm312.springquestionsanswers.shared.enums.Role;
import pl.marcinm312.springquestionsanswers.shared.testdataprovider.DateProvider;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserCreate;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserDataUpdate;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserPasswordUpdate;

import java.time.LocalDateTime;
import java.time.Month;

public class UserDataProvider {

	public static UserEntity prepareExampleGoodAdministratorWithEncodedPassword() {

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return UserEntity.builder()
				.id(1000L)
				.username("admin")
				.password(passwordEncoder.encode("password"))
				.role(Role.ROLE_ADMIN)
				.enabled(true)
				.email("admin@abc.pl")
				.createdAt(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 25, 30))
				.updatedAt(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.changePasswordDate(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.timeOfSessionExpiration(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.build();
	}

	public static UserEntity prepareExampleGoodUserWithEncodedPassword() {

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return UserEntity.builder()
				.id(1001L)
				.username("user")
				.password(passwordEncoder.encode("password"))
				.role(Role.ROLE_USER)
				.enabled(true)
				.email("test@abc.pl")
				.createdAt(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 25, 30))
				.updatedAt(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.changePasswordDate(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.timeOfSessionExpiration(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.build();
	}

	public static UserEntity prepareExampleSecondGoodUserWithEncodedPassword() {

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return UserEntity.builder()
				.id(1002L)
				.username("user2")
				.password(passwordEncoder.encode("password"))
				.role(Role.ROLE_USER)
				.enabled(true)
				.email("test2@abc.pl")
				.createdAt(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 25, 30))
				.updatedAt(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.changePasswordDate(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.timeOfSessionExpiration(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.build();
	}

	public static UserEntity prepareExampleDisabledUserWithEncodedPassword() {

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return UserEntity.builder()
				.id(1001L)
				.username("user")
				.password(passwordEncoder.encode("password"))
				.role(Role.ROLE_USER)
				.enabled(false)
				.email("test@abc.pl")
				.createdAt(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 25, 30))
				.updatedAt(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.changePasswordDate(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.timeOfSessionExpiration(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.build();
	}

	public static UserEntity prepareExampleSecondDisabledUserWithEncodedPassword() {

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return UserEntity.builder()
				.id(1003L)
				.username("user3")
				.password(passwordEncoder.encode("password"))
				.role(Role.ROLE_USER)
				.enabled(false)
				.email("test@abc.pl")
				.createdAt(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 25, 30))
				.updatedAt(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.changePasswordDate(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.timeOfSessionExpiration(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 30, 30))
				.build();
	}

	public static UserEntity prepareExampleGoodUserWithEncodedPasswordWithSpaces() {

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return UserEntity.builder()
				.id(1003L)
				.username("user3")
				.password(passwordEncoder.encode(" pas "))
				.role(Role.ROLE_USER)
				.enabled(true)
				.email("test3@abc.pl")
				.createdAt(DateProvider.prepareDate(2020, Month.JANUARY, 15, 10, 25, 30))
				.updatedAt(DateProvider.prepareDate(2020, Month.JANUARY, 15, 10, 30, 30))
				.changePasswordDate(DateProvider.prepareDate(2020, Month.JANUARY, 15, 10, 30, 30))
				.timeOfSessionExpiration(DateProvider.prepareDate(2020, Month.JANUARY, 15, 10, 30, 30))
				.build();
	}

	public static UserEntity prepareExampleGoodUserWithEncodedAndChangedPassword() {

		LocalDateTime futureDate = LocalDateTime.now().plusMinutes(10);
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

		return UserEntity.builder()
				.id(1004L)
				.username("user4")
				.password(passwordEncoder.encode("password"))
				.role(Role.ROLE_USER)
				.enabled(true)
				.email("test4@abc.pl")
				.createdAt(DateProvider.prepareDate(2020, Month.JANUARY, 10, 10, 25, 30))
				.updatedAt(futureDate)
				.changePasswordDate(futureDate)
				.timeOfSessionExpiration(futureDate)
				.build();
	}

	public static UserCreate prepareGoodUserToRequest() {
		return new UserCreate("user", "password", "password", "test@abc.pl");
	}

	public static UserCreate prepareGoodUserWithActivationUrlToRequest() {
		return new UserCreate("user", "password", "password", "test@abc.pl", "http://localhost:3000/api/token?value=");
	}

	public static UserCreate prepareGoodUserWithIncorrectActivationUrlToRequest() {
		return new UserCreate("user", "password", "password", "test@abc.pl", "incorrectUrl");
	}

	public static UserCreate prepareUserWithConfirmPasswordErrorToRequest() {
		return new UserCreate("user", "password", "anotherPassword", "test@abc.pl");
	}

	public static UserCreate prepareIncorrectUserToRequest() {
		return new UserCreate("aa", "pass", "pass", "email");
	}

	public static UserCreate prepareEmptyUserToRequest() {
		return new UserCreate("", "", "", "");
	}

	public static UserCreate prepareUserWithTooShortLoginAfterTrimToRequest() {
		return new UserCreate(" a ", "password", "password", "test@abc.pl");
	}

	public static UserCreate prepareUserWithSpacesInPasswordToRequest() {
		return new UserCreate("user", " pas ", " pas ", "test@abc.pl");
	}

	public static UserDataUpdate prepareGoodUserDataUpdateWithoutChangesToRequest() {
		return new UserDataUpdate("user", "test@abc.pl");
	}

	public static UserDataUpdate prepareGoodUserDataUpdateWithLoginChangeToRequest() {
		return new UserDataUpdate("user3", "test@abc.pl");
	}

	public static UserDataUpdate prepareGoodUserDataUpdateWithEmailChangeToRequest() {
		return new UserDataUpdate("user", "changed@abc.pl");
	}

	public static UserDataUpdate prepareGoodUserDataUpdateWithLoginAndEmailChangeToRequest() {
		return new UserDataUpdate("user3", "changed@abc.pl");
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
		return new UserPasswordUpdate("password", "pass", "pass");
	}

	public static UserPasswordUpdate prepareEmptyUserPasswordUpdateToRequest() {
		return new UserPasswordUpdate("", "", "");
	}

	public static UserPasswordUpdate prepareUserPasswordUpdateWithSpacesInPassToRequest() {
		return new UserPasswordUpdate(" pas ", " pa  ", " pa  ");
	}
}
