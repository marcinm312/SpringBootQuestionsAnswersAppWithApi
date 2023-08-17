package pl.marcinm312.springquestionsanswers.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springquestionsanswers.user.model.ActivationTokenEntity;

import java.util.Optional;

@Repository
public interface ActivationTokenRepo extends JpaRepository<ActivationTokenEntity, Long> {

	@Query("SELECT t FROM ActivationTokenEntity t LEFT JOIN FETCH t.user WHERE t.value = :value")
	Optional<ActivationTokenEntity> findByValue(@Param("value") String value);
}
