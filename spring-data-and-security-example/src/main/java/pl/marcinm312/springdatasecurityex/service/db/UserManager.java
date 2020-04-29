package pl.marcinm312.springdatasecurityex.service.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;

@Service
public class UserManager {

	private UserRepo userRepo;
	private PasswordEncoder passwordEncoder;

	@Autowired
	public UserManager(UserRepo userRepo, PasswordEncoder passwordEncoder) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
	}

	public User getUserByAuthentication(Authentication authentication) {
		String userName = authentication.getName();
		return userRepo.findByUsername(userName).get();
	}

	public void addUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setEnabled(true);
		user.setRole(Roles.ROLE_USER.name());
		userRepo.save(user);
	}
}
