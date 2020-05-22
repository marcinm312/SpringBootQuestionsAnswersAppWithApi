package pl.marcinm312.springdatasecurityex.service.db;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import javax.mail.MessagingException;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.exception.TokenNotFoundException;
import pl.marcinm312.springdatasecurityex.model.Token;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.MailService;
import pl.marcinm312.springdatasecurityex.service.SessionUtils;

@Service
public class UserManager {

	private UserRepo userRepo;
	private PasswordEncoder passwordEncoder;
	private TokenRepo tokenRepo;
	private MailService mailService;
	private SessionUtils sessionUtils;

	protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

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
		log.info("Loading user by authentication name = " + userName);
		return userRepo.findByUsername(userName).get();
	}

	public void addUser(User user, boolean isEnabled, String appURL) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setEnabled(isEnabled);
		user.setRole(Roles.ROLE_USER.name());
		log.info("Creating user = " + user.toString());
		userRepo.save(user);
		sendToken(user, appURL);
		log.info("User created");
	}

	public void updateUserData(User user, Authentication authentication) {
		log.info("Updating user");
		User oldUser = getUserByAuthentication(authentication);
		log.info("Old user = " + oldUser.toString());
		String oldUserName = oldUser.getUsername();
		user.setId(oldUser.getId());
		user.setPassword(oldUser.getPassword());
		user.setRole(oldUser.getRole());
		user.setEnabled(true);
		log.info("New user = " + user);
		userRepo.save(user);
		log.info("User updated");
		if (!oldUserName.equals(user.getUsername())) {
			Collection<? extends GrantedAuthority> updatedAuthorities = user.getAuthorities();
			Authentication newAuth = new UsernamePasswordAuthenticationToken(user.getUsername(),
					authentication.getCredentials(), updatedAuthorities);
			SecurityContextHolder.getContext().setAuthentication(newAuth);
			sessionUtils.expireUserSessions(oldUserName, false);
			sessionUtils.expireUserSessions(user.getUsername(), false);
		}
	}

	public void updateUserPassword(User user, Authentication authentication) {
		log.info("Updating user");
		User oldUser = getUserByAuthentication(authentication);
		log.info("Old user = " + oldUser.toString());
		user.setId(oldUser.getId());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole(oldUser.getRole());
		user.setEnabled(true);
		log.info("New user = " + user);
		userRepo.save(user);
		log.info("User updated");
		sessionUtils.expireUserSessions(user.getUsername(), false);
	}

	public void deleteUser(Authentication authentication) {
		User user = getUserByAuthentication(authentication);
		log.info("Deleting user = " + user.toString());
		userRepo.delete(user);
		log.info("User deleted");
		sessionUtils.expireUserSessions(authentication.getName(), true);
	}

	public void activateUser(String tokenValue) {
		Optional<Token> optionalToken = tokenRepo.findByValue(tokenValue);
		if (optionalToken.isPresent()) {
			Token token = optionalToken.get();
			User user = token.getUser();
			log.info("Activating user = " + user.toString());
			user.setEnabled(true);
			userRepo.save(user);
			tokenRepo.delete(token);
			log.info("User activated");
		} else {
			throw new TokenNotFoundException();
		}
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
			e.printStackTrace();
		}
	}

	private String generateEmailContent(User user, String tokenValue, String appURL) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Witaj " + user.getFirstName() + " " + user.getLastName() + ",");
		stringBuilder.append("<br/><br/>Potwierdź swój adres email, klikając w poniższy link:");
		stringBuilder.append("<br/><a href=\"" + appURL + "/token?value=" + tokenValue + "\">Aktywuj konto</a>");
		return stringBuilder.toString();
	}
}
