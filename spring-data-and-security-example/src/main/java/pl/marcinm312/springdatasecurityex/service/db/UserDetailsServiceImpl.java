package pl.marcinm312.springdatasecurityex.service.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
		return userRepo.findByUsername(username).get();
	}
}
