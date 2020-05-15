package pl.marcinm312.springdatasecurityex.service.db;

import java.util.Optional;

import org.slf4j.LoggerFactory;
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

	protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public UserDetailsServiceImpl(UserRepo userRepo) {
		this.userRepo = userRepo;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> optionalUser = userRepo.findByUsername(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			log.info("Loading user = " + user.toString());
			return user;
		} else {
			throw new UsernameNotFoundException("User not found");
		}
	}
}
