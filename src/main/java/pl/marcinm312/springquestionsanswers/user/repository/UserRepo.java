package pl.marcinm312.springquestionsanswers.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<UserEntity, Long> {

	default UserEntity getUserFromAuthentication(Authentication authentication) {
		return (UserEntity) authentication.getPrincipal();
	}

	Optional<UserEntity> findByUsername(String username);
}
