package pl.marcinm312.springquestionsanswers.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springquestionsanswers.user.model.MailChangeTokenEntity;

import java.util.Optional;

@Repository
public interface MailChangeTokenRepo extends JpaRepository<MailChangeTokenEntity, Long> {

	Optional<MailChangeTokenEntity> findByValue(String value);
}
