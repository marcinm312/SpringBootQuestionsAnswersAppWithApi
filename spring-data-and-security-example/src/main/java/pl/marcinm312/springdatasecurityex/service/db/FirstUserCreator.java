package pl.marcinm312.springdatasecurityex.service.db;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;

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
	public User addFirstUser() {
		String login = "administrator";
		if (!userRepo.findByUsername(login).isPresent()) {
			String password = environment.getProperty("admin.default.password");
			String email = environment.getProperty("admin.default.email");
			User user = new User();
			user.setUsername(login);
			user.setPassword(passwordEncoder.encode(password));
			user.setRole(Roles.ROLE_ADMIN.name());
			user.setEnabled(true);
			user.setEmail(email);
			User savedUser = userRepo.save(user);
			log.info("First user created");
			return savedUser;
		} else {
			return null;
		}
	}
}
