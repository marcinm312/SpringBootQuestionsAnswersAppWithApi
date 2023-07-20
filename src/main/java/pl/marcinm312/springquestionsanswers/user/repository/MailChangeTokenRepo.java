package pl.marcinm312.springquestionsanswers.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springquestionsanswers.user.model.MailChangeTokenEntity;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;

import java.util.Optional;

@Repository
public interface MailChangeTokenRepo extends JpaRepository<MailChangeTokenEntity, Long> {

	@Query("SELECT t FROM MailChangeTokenEntity t LEFT JOIN FETCH t.user WHERE t.value = :value AND t.user.username = :username")
	Optional<MailChangeTokenEntity> findByValueAndUsername(@Param("value") String value, @Param("username") String username);

	@Modifying
	@Query("DELETE FROM MailChangeTokenEntity t WHERE t.user = :user")
	void deleteByUser(@Param("user")UserEntity user);
}
