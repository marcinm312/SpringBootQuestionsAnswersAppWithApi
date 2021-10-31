package pl.marcinm312.springdatasecurityex.service.db;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.exception.TokenNotFoundException;
import pl.marcinm312.springdatasecurityex.model.user.Token;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.model.user.UserMapper;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserCreate;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserDataUpdate;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserGet;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserPasswordUpdate;
import pl.marcinm312.springdatasecurityex.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.MailService;
import pl.marcinm312.springdatasecurityex.utils.SessionUtils;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserManager {

	private final UserRepo userRepo;
	private final PasswordEncoder passwordEncoder;
	private final TokenRepo tokenRepo;
	private final MailService mailService;
	private final SessionUtils sessionUtils;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public UserManager(UserRepo userRepo, PasswordEncoder passwordEncoder, TokenRepo tokenRepo, MailService mailService,
					   SessionUtils sessionUtils) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.tokenRepo = tokenRepo;
		this.mailService = mailService;
		this.sessionUtils = sessionUtils;
	}

	public User getUserByAuthentication(Authentication authentication) {
		String userName = authentication.getName();
		log.info("Loading user by authentication name = {}", userName);
		Optional<User> optionalUser = userRepo.findByUsername(userName);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			log.info("Loaded user = {}", user);
			return user;
		} else {
			log.error("User {} not found!", userName);
			return null;
		}
	}

	public UserGet getUserDTOByAuthentication(Authentication authentication) {
		User user = getUserByAuthentication(authentication);
		return UserMapper.convertUserToUserGet(user);
	}

	@Transactional
	public UserGet addUser(UserCreate userRequest) {
		User user = new User(userRequest.getUsername(), userRequest.getPassword(), userRequest.getEmail());
		Date currentDate = new Date();
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setEnabled(false);
		user.setRole(Roles.ROLE_USER.name());
		user.setTimeOfSessionExpiration(currentDate);
		user.setChangePasswordDate(currentDate);
		log.info("Creating user = {}", user);
		User savedUser = userRepo.save(user);
		sendToken(user);
		log.info("User created");
		return UserMapper.convertUserToUserGet(savedUser);
	}

	@Transactional
	public UserGet updateUserData(UserDataUpdate userRequest, Authentication authentication) {
		log.info("Updating user");
		User loggedUser = getUserByAuthentication(authentication);
		log.info("Old user = {}", loggedUser);
		String oldUserName = loggedUser.getUsername();
		if (!oldUserName.equals(userRequest.getUsername())) {
			loggedUser = sessionUtils.expireUserSessions(loggedUser, true, false);
		}
		loggedUser.setUsername(userRequest.getUsername());
		loggedUser.setEmail(userRequest.getEmail());
		log.info("New user = {}", loggedUser);
		User savedUser = userRepo.save(loggedUser);
		log.info("User updated");
		return UserMapper.convertUserToUserGet(savedUser);
	}

	@Transactional
	public UserGet updateUserPassword(UserPasswordUpdate userRequest, Authentication authentication) {
		log.info("Updating user password");
		Date currentDate = new Date();
		User loggedUser = getUserByAuthentication(authentication);
		log.info("Old user = {}", loggedUser);
		loggedUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		loggedUser.setChangePasswordDate(currentDate);
		loggedUser = sessionUtils.expireUserSessions(loggedUser, false, false);
		log.info("New user = {}", loggedUser);
		User savedUser = userRepo.save(loggedUser);
		log.info("User password updated");
		return UserMapper.convertUserToUserGet(savedUser);
	}

	@Transactional
	public void deleteUser(Authentication authentication) {
		User user = getUserByAuthentication(authentication);
		log.info("Deleting user = {}", user);
		userRepo.delete(user);
		log.info("User deleted");
		sessionUtils.expireUserSessions(user, true, true);
	}

	@Transactional
	public User activateUser(String tokenValue) {
		Optional<Token> optionalToken = tokenRepo.findByValue(tokenValue);
		if (optionalToken.isPresent()) {
			Token token = optionalToken.get();
			User user = token.getUser();
			log.info("Activating user = {}", user);
			user.setEnabled(true);
			User savedUser = userRepo.save(user);
			tokenRepo.delete(token);
			log.info("User activated");
			return savedUser;
		} else {
			throw new TokenNotFoundException();
		}
	}

	@Transactional
	public UserGet endOtherSessions(Authentication authentication) {
		log.info("Expiring sessions for user = {}", authentication.getName());
		User user = getUserByAuthentication(authentication);
		user = sessionUtils.expireUserSessions(user, false, false);
		User savedUser = userRepo.save(user);
		log.info("Sessions for user expired");
		return UserMapper.convertUserToUserGet(savedUser);
	}

	private void sendToken(User user) {
		String tokenValue = UUID.randomUUID().toString();
		Token token = new Token();
		token.setUser(user);
		token.setValue(tokenValue);
		tokenRepo.save(token);
		String emailContent = generateEmailContent(user, tokenValue);
		try {
			mailService.sendMail(user.getEmail(), "Potwierdź swój adres email", emailContent, true);
		} catch (MessagingException e) {
			log.error("An error occurred while sending the email. [MESSAGE]: {}", e.getMessage());
		}
	}

	private String generateEmailContent(User user, String tokenValue) {
		return new StringBuilder().append("Witaj ").append(user.getUsername())
				.append(",<br><br>Potwierdź swój adres email, klikając w poniższy link:")
				.append("<br><a href=\"").append(getApplicationUrl()).append("/token?value=").append(tokenValue)
				.append("\">Aktywuj konto</a>").toString();
	}

	private String getApplicationUrl() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String requestURL = request.getRequestURL().toString();
		String servletPath = request.getServletPath();
		return requestURL.replace(servletPath, "");
	}
}
