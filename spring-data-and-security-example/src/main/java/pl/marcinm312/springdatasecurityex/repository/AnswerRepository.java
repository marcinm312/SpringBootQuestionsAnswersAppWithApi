package pl.marcinm312.springdatasecurityex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springdatasecurityex.model.Answer;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
	List<Answer> findByQuestionIdOrderByIdDesc(Long questionId);
}
