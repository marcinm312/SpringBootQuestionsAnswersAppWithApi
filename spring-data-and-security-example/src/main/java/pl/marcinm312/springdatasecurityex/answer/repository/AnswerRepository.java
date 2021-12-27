package pl.marcinm312.springdatasecurityex.answer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springdatasecurityex.answer.model.AnswerEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {

	@Query("SELECT a FROM AnswerEntity a LEFT JOIN FETCH a.user WHERE a.question.id = :questionId")
	List<AnswerEntity> getAnswers(@Param("questionId") Long questionId, Sort sort);

	@Query("SELECT a FROM AnswerEntity a LEFT JOIN FETCH a.user WHERE a.question.id = :questionId AND " +
			"LOWER(CONCAT(a.id, ' ', a.text, ' ', a.createdAt, ' ', a.updatedAt, ' ', a.user.username)) LIKE %:keyword%")
	List<AnswerEntity> searchAnswers(@Param("questionId") Long questionId, @Param("keyword") String keyword, Sort sort);

	@Query(value = "SELECT a FROM AnswerEntity a LEFT JOIN FETCH a.user WHERE a.question.id = :questionId",
		countQuery = "SELECT COUNT(a) FROM AnswerEntity a LEFT JOIN a.user WHERE a.question.id = :questionId")
	Page<AnswerEntity> getPaginatedAnswers(@Param("questionId") Long questionId, Pageable pageable);

	@Query(value = "SELECT a FROM AnswerEntity a LEFT JOIN FETCH a.user WHERE a.question.id = :questionId AND " +
			"LOWER(CONCAT(a.id, ' ', a.text, ' ', a.createdAt, ' ', a.updatedAt, ' ', a.user.username)) LIKE %:keyword%",
		countQuery = "SELECT COUNT(a) FROM AnswerEntity a LEFT JOIN a.user WHERE a.question.id = :questionId AND " +
				"LOWER(CONCAT(a.id, ' ', a.text, ' ', a.createdAt, ' ', a.updatedAt, ' ', a.user.username)) LIKE %:keyword%")
	Page<AnswerEntity> searchPaginatedAnswers(@Param("questionId") Long questionId, @Param("keyword") String keyword, Pageable pageable);

	Optional<AnswerEntity> findByQuestionIdAndId(Long questionId, Long answerId);
}
