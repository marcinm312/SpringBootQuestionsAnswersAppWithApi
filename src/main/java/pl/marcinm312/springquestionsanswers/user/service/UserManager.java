package pl.marcinm312.springquestionsanswers.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.marcinm312.springquestionsanswers.config.security.utils.SessionUtils;
import pl.marcinm312.springquestionsanswers.user.model.Role;
import pl.marcinm312.springquestionsanswers.mail.service.MailSender;
import pl.marcinm312.springquestionsanswers.user.exception.TokenNotFoundException;
import pl.marcinm312.springquestionsanswers.user.exception.UserNotExistsException;
import pl.marcinm312.springquestionsanswers.user.model.ActivationTokenEntity;
import pl.marcinm312.springquestionsanswers.user.model.MailChangeTokenEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserMapper;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserCreate;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserDataUpdate;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserGet;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserPasswordUpdate;
import pl.marcinm312.springquestionsanswers.user.repository.ActivationTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.MailChangeTokenRepo;
import pl.marcinm312.springquestionsanswers.user.repository.UserRepo;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserManager {

	private final UserRepo userRepo;
	private final PasswordEncoder passwordEncoder;
	private final ActivationTokenRepo activationTokenRepo;
	private final MailChangeTokenRepo mailChangeTokenRepo;
	private final MailSender mailSender;
	private final SessionUtils sessionUtils;


	enum MailType {
		ACTIVATION,
		MAIL_CHANGE
	}

	public UserEntity getUserFromDB(Authentication authentication) {

		String userName = authentication.getName();
		log.info("Loading user by authentication name = {}", userName);
		Optional<UserEntity> optionalUser = userRepo.findByUsername(userName);
		if (optionalUser.isEmpty()) {
			log.error("User {} not found!", userName);
			throw new UserNotExistsException();
		}
		UserEntity user = optionalUser.get();
		log.info("Loaded user = {}", user);
		return user;
	}

	public UserEntity getUserFromAuthentication(Authentication authentication) {
		return userRepo.getUserFromAuthentication(authentication);
	}

	@Transactional
	public UserGet addUser(UserCreate userRequest) {

		LocalDateTime currentDate = LocalDateTime.now();
		UserEntity user = UserEntity.builder()
				.username(userRequest.getUsername())
				.password(passwordEncoder.encode(userRequest.getPassword()))
				.email(userRequest.getEmail())
				.enabled(false)
				.accountNonLocked(true)
				.role(Role.ROLE_USER)
				.timeOfSessionExpiration(currentDate)
				.changePasswordDate(currentDate)
				.build();

		log.info("Creating user = {}", user);
		UserEntity savedUser = userRepo.save(user);
		sendActivationToken(user, userRequest.getActivationUrl());
		log.info("User created");
		return UserMapper.convertUserToUserGet(savedUser, true);
	}

	@Transactional
	public UserGet updateUserData(UserDataUpdate userRequest, Authentication authentication) {

		log.info("Updating user");
		UserEntity loggedUser = getUserFromDB(authentication);
		log.info("Old user = {}", loggedUser);
		if (!loggedUser.getUsername().equals(userRequest.getUsername())) {
			log.info("Login change");
			loggedUser = sessionUtils.expireUserSessions(loggedUser, true, false);
			loggedUser.setUsername(userRequest.getUsername());
		}
		if (!loggedUser.getEmail().equals(userRequest.getEmail())) {
			log.info("Mail change");
			sendMailChangeToken(loggedUser, userRequest);
		}
		log.info("New user = {}", loggedUser);
		UserEntity savedUser = userRepo.save(loggedUser);
		log.info("User updated");
		return UserMapper.convertUserToUserGet(savedUser, true);
	}

	@Transactional
	public UserGet updateUserPassword(UserPasswordUpdate userRequest, Authentication authentication) {

		log.info("Updating user password");
		LocalDateTime currentDate = LocalDateTime.now();
		UserEntity loggedUser = getUserFromDB(authentication);
		log.info("Old user = {}", loggedUser);
		loggedUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		loggedUser.setChangePasswordDate(currentDate);
		loggedUser = sessionUtils.expireUserSessions(loggedUser, false, false);
		log.info("New user = {}", loggedUser);
		UserEntity savedUser = userRepo.save(loggedUser);
		log.info("User password updated");
		return UserMapper.convertUserToUserGet(savedUser, true);
	}

	@Transactional
	public boolean deleteUser(Authentication authentication) {

		UserEntity user = getUserFromDB(authentication);
		log.info("Deleting user = {}", user);
		userRepo.delete(user);
		log.info("User deleted");
		sessionUtils.expireUserSessions(user, true, true);
		return true;
	}

	@Transactional
	public UserGet activateUser(String tokenValue) {

		log.info("Token value = {}", tokenValue);
		Optional<ActivationTokenEntity> optionalToken = activationTokenRepo.findByValue(tokenValue);
		if (optionalToken.isEmpty()) {
			throw new TokenNotFoundException();
		}
		ActivationTokenEntity token = optionalToken.get();
		UserEntity user = token.getUser();
		log.info("Activating user = {}", user);
		user.setEnabled(true);
		UserEntity savedUser = userRepo.save(user);
		activationTokenRepo.delete(token);
		log.info("User activated");
		return UserMapper.convertUserToUserGet(savedUser, true);
	}

	@Transactional
	public UserGet confirmMailChange(String tokenValue, Authentication authentication) {

		String userName = authentication.getName();
		log.info("Token value = {}, userName={}", tokenValue, userName);
		Optional<MailChangeTokenEntity> optionalToken = mailChangeTokenRepo.findByValueAndUsername(tokenValue, userName);
		if (optionalToken.isEmpty()) {
			throw new TokenNotFoundException();
		}
		MailChangeTokenEntity token = optionalToken.get();
		UserEntity user = token.getUser();
		log.info("Changing mail for user = {}", user);
		user.setEmail(token.getNewEmail());
		UserEntity savedUser = userRepo.save(user);
		mailChangeTokenRepo.deleteByUser(user);
		log.info("Mail change confirmed");
		return UserMapper.convertUserToUserGet(savedUser, true);
	}

	@Transactional
	public UserGet expireOtherSessions(Authentication authentication) {

		log.info("Expiring sessions for user = {}", authentication.getName());
		UserEntity user = getUserFromDB(authentication);
		user = sessionUtils.expireUserSessions(user, false, false);
		UserEntity savedUser = userRepo.save(user);
		log.info("Sessions for user expired");
		return UserMapper.convertUserToUserGet(savedUser, true);
	}

	private void sendActivationToken(UserEntity user, String activationUrl) {

		String tokenValue = UUID.randomUUID().toString();
		ActivationTokenEntity token = new ActivationTokenEntity(tokenValue, user);
		activationTokenRepo.save(token);
		String emailContent = generateActivationEmailContent(user, tokenValue, activationUrl);
		mailSender.sendMail(user.getEmail(), "Potwierdź swój adres email", emailContent, true);
	}

	private String generateActivationEmailContent(UserEntity user, String tokenValue, String activationUrl) {

		String mailTemplate =
				"""
						Witaj %s,<br>
						<br>Potwierdź swój adres email, klikając w poniższy link:
						<br><a href="%s">Aktywuj konto</a>""";
		return String.format(mailTemplate, user.getUsername(), getTokenUrl(activationUrl, MailType.ACTIVATION, tokenValue));
	}

	private String getTokenUrl(String urlFromRequest, MailType mailType, String tokenValue) {

		if (urlFromRequest != null && urlFromRequest.startsWith("http")) {
			return urlFromRequest.trim() + tokenValue;
		}
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String requestURL = request.getRequestURL().toString();
		String servletPath = request.getServletPath();
		String applicationUrl = requestURL.replace(servletPath, "");
		String tokenUrl;
		switch(mailType) {
			case ACTIVATION -> tokenUrl = applicationUrl + "/token/?value=" + tokenValue;
			case MAIL_CHANGE -> tokenUrl = applicationUrl + "/app/myProfile/update/confirm/?value=" + tokenValue;
			default -> tokenUrl = "";
		}
		return tokenUrl;
	}

	private void sendMailChangeToken(UserEntity loggedUser, UserDataUpdate userRequest) {

		String tokenValue = UUID.randomUUID().toString();
		MailChangeTokenEntity token = new MailChangeTokenEntity(tokenValue, userRequest.getEmail(), loggedUser);
		mailChangeTokenRepo.save(token);
		String emailContent = generateMailChangeEmailContent(loggedUser, tokenValue, userRequest);
		mailSender.sendMail(loggedUser.getEmail(), "Potwierdź swój nowy adres email", emailContent, true);
	}

	private String generateMailChangeEmailContent(UserEntity user, String tokenValue, UserDataUpdate userRequest) {

		String mailTemplate =
				"""
						Witaj %s,<br>
						<br>Twój nowy adres email: <b>%s</b><br>
						<br>Potwierdź swój nowy adres email, klikając w poniższy link:
						<br><a href="%s">Potwierdzam zmianę adresu email</a>""";
		return String.format(mailTemplate, user.getUsername(), userRequest.getEmail(),
				getTokenUrl(userRequest.getConfirmUrl(), MailType.MAIL_CHANGE, tokenValue));
	}
}
