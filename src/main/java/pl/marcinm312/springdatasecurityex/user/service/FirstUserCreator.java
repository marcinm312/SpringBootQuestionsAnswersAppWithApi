package pl.marcinm312.springdatasecurityex.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.marcinm312.springdatasecurityex.shared.enums.Role;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.repository.UserRepo;

import java.util.Date;

@RequiredArgsConstructor
@Slf4j
@Service
public class FirstUserCreator {

	private final UserRepo userRepo;
	private final PasswordEncoder passwordEncoder;
	private final Environment environment;


	@EventListener(ApplicationReadyEvent.class)
	public UserEntity addFirstUser() {
		String login = "administrator";
		if (userRepo.findByUsername(login).isEmpty()) {
			String password = environment.getProperty("admin.default.password");
			String email = environment.getProperty("admin.default.email");
			Date currentDate = new Date();

			UserEntity user = UserEntity.builder()
					.username(login)
					.password(passwordEncoder.encode(password))
					.role(Role.ROLE_ADMIN)
					.enabled(true)
					.email(email)
					.timeOfSessionExpiration(currentDate)
					.changePasswordDate(currentDate)
					.build();

			UserEntity savedUser = userRepo.save(user);
			log.info("First user created");
			return savedUser;
		} else {
			return null;
		}
	}
}
