package pl.marcinm312.springdatasecurityex.answer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springdatasecurityex.answer.model.AnswerEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {

	@Query("SELECT a FROM AnswerEntity a LEFT JOIN FETCH a.user WHERE a.question.id = :questionId ORDER BY a.id DESC")
	List<AnswerEntity> findByQuestionIdOrderByIdDesc(@Param("questionId") Long questionId);

	Optional<AnswerEntity> findByQuestionIdAndId(Long questionId, Long answerId);
}
