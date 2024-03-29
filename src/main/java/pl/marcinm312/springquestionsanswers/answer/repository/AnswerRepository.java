package pl.marcinm312.springquestionsanswers.answer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springquestionsanswers.answer.model.AnswerEntity;

import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {

	@Query(value = "SELECT a FROM AnswerEntity a LEFT JOIN FETCH a.user WHERE a.question.id = :questionId",
		countQuery = "SELECT COUNT(a) FROM AnswerEntity a LEFT JOIN a.user WHERE a.question.id = :questionId")
	Page<AnswerEntity> getPaginatedAnswers(@Param("questionId") Long questionId, Pageable pageable);

	@Query(value = "SELECT a FROM AnswerEntity a LEFT JOIN FETCH a.user WHERE a.question.id = :questionId AND " +
			"LOWER(CONCAT(a.id, ' ', a.text, ' ', a.createdAt, ' ', a.updatedAt, ' ', a.user.username)) LIKE %:keyword%",
		countQuery = "SELECT COUNT(a) FROM AnswerEntity a LEFT JOIN a.user WHERE a.question.id = :questionId AND " +
				"LOWER(CONCAT(a.id, ' ', a.text, ' ', a.createdAt, ' ', a.updatedAt, ' ', a.user.username)) LIKE %:keyword%")
	Page<AnswerEntity> searchPaginatedAnswers(@Param("questionId") Long questionId, @Param("keyword") String keyword, Pageable pageable);

	@Query("SELECT a FROM AnswerEntity a LEFT JOIN FETCH a.user WHERE a.id = :answerId AND a.question.id = :questionId")
	Optional<AnswerEntity> findByQuestionIdAndId(@Param("questionId") Long questionId, @Param("answerId") Long answerId);
}
