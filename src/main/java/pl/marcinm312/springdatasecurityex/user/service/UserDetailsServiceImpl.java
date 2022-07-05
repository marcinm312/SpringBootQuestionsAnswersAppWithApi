package pl.marcinm312.springdatasecurityex.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.repository.UserRepo;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepo userRepo;


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<UserEntity> optionalUser = userRepo.findByUsername(username);
		return getUserFromOptional(optionalUser);
	}

	public UserEntity findUserById(Long userId) {
		Optional<UserEntity> optionalUser = userRepo.findById(userId);
		return getUserFromOptional(optionalUser);
	}

	private UserEntity getUserFromOptional(Optional<UserEntity> optionalUser) {
		if (optionalUser.isPresent()) {
			UserEntity user = optionalUser.get();
			log.info("Loading user = {}", user);
			return user;
		} else {
			log.error("User not found!");
			throw new UsernameNotFoundException("Nie znaleziono użytkownika");
		}
	}

	public Optional<UserEntity> findUserByUsername(String username) {
		return userRepo.findByUsername(username);
	}
}
