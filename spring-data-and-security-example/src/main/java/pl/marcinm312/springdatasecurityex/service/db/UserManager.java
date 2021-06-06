package pl.marcinm312.springdatasecurityex.service.db;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.exception.IllegalLoginChange;
import pl.marcinm312.springdatasecurityex.exception.TokenNotFoundException;
import pl.marcinm312.springdatasecurityex.model.user.Token;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.model.user.UserMapper;
import pl.marcinm312.springdatasecurityex.model.user.dto.UserGet;
import pl.marcinm312.springdatasecurityex.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.MailService;
import pl.marcinm312.springdatasecurityex.utils.SessionUtils;

import javax.mail.MessagingException;
import java.util.Collection;
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
	public User addUser(User user, String appURL) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setEnabled(false);
		user.setRole(Roles.ROLE_USER.name());
		log.info("Creating user = {}", user);
		User savedUser = userRepo.save(user);
		sendToken(user, appURL);
		log.info("User created");
		return savedUser;
	}

	@Transactional
	public User updateUserData(User user, Authentication authentication) {
		log.info("Updating user");
		User oldUser = getUserByAuthentication(authentication);
		log.info("Old user = {}", oldUser);
		String oldUserName = oldUser.getUsername();
		user.setId(oldUser.getId());
		user.setPassword(oldUser.getPassword());
		user.setRole(oldUser.getRole());
		user.setEnabled(true);
		log.info("New user = {}", user);
		User savedUser = userRepo.save(user);
		log.info("User updated");
		if (!oldUserName.equals(user.getUsername())) {
			Collection<? extends GrantedAuthority> updatedAuthorities = user.getAuthorities();
			Authentication newAuth = new UsernamePasswordAuthenticationToken(user.getUsername(),
					authentication.getCredentials(), updatedAuthorities);
			SecurityContextHolder.getContext().setAuthentication(newAuth);
			sessionUtils.expireUserSessions(oldUserName, true);
			sessionUtils.expireUserSessions(user.getUsername(), true);
		}
		return savedUser;
	}

	@Transactional
	public User updateUserPassword(User user, Authentication authentication) {
		log.info("Updating user password");
		User oldUser = getUserByAuthentication(authentication);
		log.info("Old user = {}", oldUser);
		user.setId(oldUser.getId());
		if (oldUser.getUsername().equals(user.getUsername())) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user.setEmail(oldUser.getEmail());
			user.setRole(oldUser.getRole());
			user.setEnabled(true);
			log.info("New user = {}", user);
			User savedUser = userRepo.save(user);
			log.info("User password updated");
			sessionUtils.expireUserSessions(user.getUsername(), false);
			return savedUser;
		} else {
			log.error("Illegal login change!");
			throw new IllegalLoginChange();
		}
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

	private void sendToken(User user, String appURL) {
		String tokenValue = UUID.randomUUID().toString();
		Token token = new Token();
		token.setUser(user);
		token.setValue(tokenValue);
		tokenRepo.save(token);
		String emailContent = generateEmailContent(user, tokenValue, appURL);
		try {
			mailService.sendMail(user.getEmail(), "Potwierdź swój adres email", emailContent, true);
		} catch (MessagingException e) {
			log.error("An error occurred while sending the email. [MESSAGE]: {}", e.getMessage());
		}
	}

	private String generateEmailContent(User user, String tokenValue, String appURL) {
		return "Witaj " + user.getUsername() + "," + "<br><br>Potwierdź swój adres email, klikając w poniższy link:"
				+ "<br><a href=\"" + appURL + "/token?value=" + tokenValue + "\">Aktywuj konto</a>";
	}
}
