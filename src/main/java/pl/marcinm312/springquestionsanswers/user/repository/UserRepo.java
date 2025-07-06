package pl.marcinm312.springquestionsanswers.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<UserEntity, Long> {

	default UserEntity getUserFromAuthentication(Authentication authentication) {
		return (UserEntity) authentication.getPrincipal();
	}

	Optional<UserEntity> findByUsername(String username);

	@Query("SELECT u FROM UserEntity u WHERE u.enabled = false AND DATE(u.createdAt) < :dateFrom ORDER BY u.id DESC")
	List<UserEntity> getNonEnabledOldUsers(@Param("dateFrom") LocalDate dateFrom);
}
