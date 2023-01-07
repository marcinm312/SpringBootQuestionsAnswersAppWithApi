package pl.marcinm312.springquestionsanswers.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springquestionsanswers.user.model.TokenEntity;

import java.util.Optional;

@Repository
public interface TokenRepo extends JpaRepository<TokenEntity, Long> {

	Optional<TokenEntity> findByValue(String value);
}
