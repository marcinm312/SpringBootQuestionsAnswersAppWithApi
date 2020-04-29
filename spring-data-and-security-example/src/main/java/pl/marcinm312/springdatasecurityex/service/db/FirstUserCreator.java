package pl.marcinm312.springdatasecurityex.service.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;

@Service
public class FirstUserCreator {

	@Value("${admin.default.password}")
	private String password;

	@Value("${admin.default.email}")
	private String email;

	private UserRepo userRepo;
	private PasswordEncoder passwordEncoder;

	@Autowired
	public FirstUserCreator(UserRepo userRepo, PasswordEncoder passwordEncoder) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void addFirstUser() {
		String login = "administrator";
		if (!userRepo.findByUsername(login).isPresent()) {
			User user = new User();
			user.setUsername(login);
			user.setPassword(passwordEncoder.encode(password));
			user.setRole(Roles.ROLE_ADMIN.name());
			user.setEnabled(true);
			user.setFirstName("System");
			user.setLastName("Admin");
			user.setEmail(email);
			userRepo.save(user);
		}
	}
}