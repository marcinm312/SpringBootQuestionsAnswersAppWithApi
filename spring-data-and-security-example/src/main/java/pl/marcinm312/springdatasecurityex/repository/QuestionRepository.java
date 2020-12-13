package pl.marcinm312.springdatasecurityex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springdatasecurityex.model.Question;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
	List<Question> findAllByOrderByIdDesc();
}
