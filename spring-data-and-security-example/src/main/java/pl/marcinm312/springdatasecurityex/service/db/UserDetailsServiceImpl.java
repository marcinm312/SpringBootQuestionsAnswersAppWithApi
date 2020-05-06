package pl.marcinm312.springdatasecurityex.service.db;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.UserRepo;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private UserRepo userRepo;

	@Autowired
	public UserDetailsServiceImpl(UserRepo userRepo) {
		this.userRepo = userRepo;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> user = userRepo.findByUsername(username);
		if (user.isPresent()) {
			return user.get();
		} else {
			throw new UsernameNotFoundException("User not found");
		}
	}
}
