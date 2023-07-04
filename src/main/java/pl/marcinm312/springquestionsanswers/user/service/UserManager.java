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
import pl.marcinm312.springquestionsanswers.shared.enums.Role;
import pl.marcinm312.springquestionsanswers.shared.mail.MailService;
import pl.marcinm312.springquestionsanswers.user.exception.TokenNotFoundException;
import pl.marcinm312.springquestionsanswers.user.model.ActivationTokenEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserMapper;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserCreate;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserDataUpdate;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserGet;
import pl.marcinm312.springquestionsanswers.user.model.dto.UserPasswordUpdate;
import pl.marcinm312.springquestionsanswers.user.repository.ActivationTokenRepo;
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
	private final MailService mailService;
	private final SessionUtils sessionUtils;


	public UserEntity getUserFromDB(Authentication authentication) {

		String userName = authentication.getName();
		log.info("Loading user by authentication name = {}", userName);
		Optional<UserEntity> optionalUser = userRepo.findByUsername(userName);
		if (optionalUser.isPresent()) {
			UserEntity user = optionalUser.get();
			log.info("Loaded user = {}", user);
			return user;
		}
		log.error("User {} not found!", userName);
		return null;
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
				.role(Role.ROLE_USER)
				.timeOfSessionExpiration(currentDate)
				.changePasswordDate(currentDate)
				.build();

		log.info("Creating user = {}", user);
		UserEntity savedUser = userRepo.save(user);
		sendToken(user, userRequest.getActivationUrl());
		log.info("User created");
		return UserMapper.convertUserToUserGet(savedUser, true);
	}

	@Transactional
	public UserGet updateUserData(UserDataUpdate userRequest, Authentication authentication) {

		log.info("Updating user");
		UserEntity loggedUser = getUserFromDB(authentication);
		log.info("Old user = {}", loggedUser);
		String oldUserName = loggedUser.getUsername();
		if (!oldUserName.equals(userRequest.getUsername())) {
			loggedUser = sessionUtils.expireUserSessions(loggedUser, true, false);
		}
		loggedUser.setUsername(userRequest.getUsername());
		loggedUser.setEmail(userRequest.getEmail());
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
	public UserGet expireOtherSessions(Authentication authentication) {

		log.info("Expiring sessions for user = {}", authentication.getName());
		UserEntity user = getUserFromDB(authentication);
		user = sessionUtils.expireUserSessions(user, false, false);
		UserEntity savedUser = userRepo.save(user);
		log.info("Sessions for user expired");
		return UserMapper.convertUserToUserGet(savedUser, true);
	}

	private void sendToken(UserEntity user, String activationUrl) {

		String tokenValue = UUID.randomUUID().toString();
		ActivationTokenEntity token = new ActivationTokenEntity(tokenValue, user);
		activationTokenRepo.save(token);
		String emailContent = generateEmailContent(user, tokenValue, activationUrl);
		mailService.sendMail(user.getEmail(), "Potwierdź swój adres email", emailContent, true);
	}

	private String generateEmailContent(UserEntity user, String tokenValue, String activationUrl) {

		String mailTemplate =
				"""
						Witaj %s,<br>
						<br>Potwierdź swój adres email, klikając w poniższy link:
						<br><a href="%s">Aktywuj konto</a>""";
		return String.format(mailTemplate, user.getUsername(), getTokenUrl(activationUrl) + tokenValue);
	}

	private String getTokenUrl(String activationUrl) {

		if (activationUrl != null && activationUrl.startsWith("http")) {
			return activationUrl.trim();
		}
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String requestURL = request.getRequestURL().toString();
		String servletPath = request.getServletPath();
		return requestURL.replace(servletPath, "") + "/token/?value=";
	}
}
