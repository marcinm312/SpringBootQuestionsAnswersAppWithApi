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
		return optionalUser.orElse(null);
	}

	public UserGet getUserDTOByAuthentication(Authentication authentication) {
		User user = getUserByAuthentication(authentication);
		if (user != null) {
			return UserMapper.convertUserToUserGet(user);
		} else {
			return null;
		}
	}

	@Transactional
	public UserGet addUser(UserCreate userRequest) {
		User user = new User(userRequest.getUsername(), userRequest.getPassword(), userRequest.getEmail());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setEnabled(false);
		user.setRole(Roles.ROLE_USER.name());
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
		loggedUser.setUsername(userRequest.getUsername());
		loggedUser.setEmail(userRequest.getEmail());
		log.info("New user = {}", loggedUser);
		User savedUser = userRepo.save(loggedUser);
		log.info("User updated");
		if (!oldUserName.equals(userRequest.getUsername())) {
			sessionUtils.expireUserSessions(oldUserName, true);
			sessionUtils.expireUserSessions(userRequest.getUsername(), true);
		}
		return UserMapper.convertUserToUserGet(savedUser);
	}

	@Transactional
	public UserGet updateUserPassword(UserPasswordUpdate userRequest, Authentication authentication) {
		log.info("Updating user password");
		User loggedUser = getUserByAuthentication(authentication);
		log.info("Old user = {}", loggedUser);
		loggedUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		log.info("New user = {}", loggedUser);
		User savedUser = userRepo.save(loggedUser);
		log.info("User password updated");
		sessionUtils.expireUserSessions(loggedUser.getUsername(), false);
		return UserMapper.convertUserToUserGet(savedUser);
	}

	@Transactional
	public void deleteUser(Authentication authentication) {
		User user = getUserByAuthentication(authentication);
		log.info("Deleting user = {}", user);
		userRepo.delete(user);
		log.info("User deleted");
		log.info("Expiring session for user: {}", authentication.getName());
		sessionUtils.expireUserSessions(authentication.getName(), true);
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

	public Optional<User> findUserByUsername(String username) {
		return userRepo.findByUsername(username);
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
