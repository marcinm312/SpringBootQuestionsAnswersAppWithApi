package pl.marcinm312.springdatasecurityex.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springdatasecurityex.question.model.Question;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

	@Query("SELECT q FROM Question q LEFT JOIN FETCH q.user ORDER BY q.id DESC")
	List<Question> findAllByOrderByIdDesc();
}
