package pl.marcinm312.springdatasecurityex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.marcinm312.springdatasecurityex.model.Token;

@Repository
public interface TokenRepo extends JpaRepository<Token, Long> {

	Token findByValue(String value);
}
