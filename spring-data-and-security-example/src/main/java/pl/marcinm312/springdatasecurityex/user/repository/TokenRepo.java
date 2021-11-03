package pl.marcinm312.springdatasecurityex.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.marcinm312.springdatasecurityex.user.model.Token;

@Repository
public interface TokenRepo extends JpaRepository<Token, Long> {

	Optional<Token> findByValue(String value);
}
