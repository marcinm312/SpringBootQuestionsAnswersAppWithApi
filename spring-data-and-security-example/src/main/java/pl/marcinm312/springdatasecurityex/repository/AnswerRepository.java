package pl.marcinm312.springdatasecurityex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springdatasecurityex.model.answer.Answer;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

	@Query("SELECT a FROM Answer a LEFT JOIN FETCH a.user WHERE a.question.id = :questionId ORDER BY a.id DESC")
	List<Answer> findByQuestionIdOrderByIdDesc(@Param("questionId") Long questionId);
}
