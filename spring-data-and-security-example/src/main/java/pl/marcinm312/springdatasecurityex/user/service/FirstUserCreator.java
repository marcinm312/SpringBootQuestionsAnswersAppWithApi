package pl.marcinm312.springdatasecurityex.user.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.marcinm312.springdatasecurityex.shared.enums.Roles;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.repository.UserRepo;

import java.util.Date;

@Service
public class FirstUserCreator {

	private final UserRepo userRepo;
	private final PasswordEncoder passwordEncoder;
	private final Environment environment;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public FirstUserCreator(UserRepo userRepo, PasswordEncoder passwordEncoder, Environment environment) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.environment = environment;
	}

	@EventListener(ApplicationReadyEvent.class)
	public UserEntity addFirstUser() {
		String login = "administrator";
		if (!userRepo.findByUsername(login).isPresent()) {
			String password = environment.getProperty("admin.default.password");
			String email = environment.getProperty("admin.default.email");
			Date currentDate = new Date();
			UserEntity user = new UserEntity();
			user.setUsername(login);
			user.setPassword(passwordEncoder.encode(password));
			user.setRole(Roles.ROLE_ADMIN.name());
			user.setEnabled(true);
			user.setEmail(email);
			user.setTimeOfSessionExpiration(currentDate);
			user.setChangePasswordDate(currentDate);
			UserEntity savedUser = userRepo.save(user);
			log.info("First user created");
			return savedUser;
		} else {
			return null;
		}
	}
}
