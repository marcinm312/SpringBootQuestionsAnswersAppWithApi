package pl.marcinm312.springdatasecurityex.service.db;

import java.util.UUID;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.model.Token;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.TokenRepo;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;
import pl.marcinm312.springdatasecurityex.service.MailService;

@Service
public class UserManager {

	private UserRepo userRepo;
	private PasswordEncoder passwordEncoder;
	private TokenRepo tokenRepo;
	private MailService mailService;

	@Autowired
	public UserManager(UserRepo userRepo, PasswordEncoder passwordEncoder, TokenRepo tokenRepo,
			MailService mailService) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.tokenRepo = tokenRepo;
		this.mailService = mailService;
	}

	public User getUserByAuthentication(Authentication authentication) {
		String userName = authentication.getName();
		return userRepo.findByUsername(userName).get();
	}

	public void addUser(User user, boolean isEnabled, String appURL) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setEnabled(isEnabled);
		user.setRole(Roles.ROLE_USER.name());
		userRepo.save(user);
		sendToken(user, appURL);
	}

	public void activateUser(String tokenValue) {
		Token token = tokenRepo.findByValue(tokenValue);
		User user = token.getUser();
		user.setEnabled(true);
		userRepo.save(user);
	}

	private void sendToken(User user, String appURL) {
		String tokenValue = UUID.randomUUID().toString();
		Token token = new Token();
		token.setValue(tokenValue);
		token.setUser(user);
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
