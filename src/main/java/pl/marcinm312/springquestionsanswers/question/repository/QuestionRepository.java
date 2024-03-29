package pl.marcinm312.springquestionsanswers.question.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springquestionsanswers.question.model.QuestionEntity;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

	@Query(value = "SELECT q FROM QuestionEntity q LEFT JOIN FETCH q.user",
		countQuery = "SELECT COUNT(q) FROM QuestionEntity q LEFT JOIN q.user")
	Page<QuestionEntity> getPaginatedQuestions(Pageable pageable);

	@Query(value = "SELECT q FROM QuestionEntity q LEFT JOIN FETCH q.user " +
			"WHERE LOWER(CONCAT(q.id, ' ', q.title, ' ', q.description, ' ', q.createdAt, ' ', q.updatedAt, ' ', q.user.username)) LIKE %:keyword%",
		countQuery = "SELECT COUNT(q) FROM QuestionEntity q LEFT JOIN q.user " +
				"WHERE LOWER(CONCAT(q.id, ' ', q.title, ' ', q.description, ' ', q.createdAt, ' ', q.updatedAt, ' ', q.user.username)) LIKE %:keyword%")
	Page<QuestionEntity> searchPaginatedQuestions(@Param("keyword") String keyword, Pageable pageable);

	@Query("SELECT q FROM QuestionEntity q LEFT JOIN FETCH q.user WHERE q.id = :questionId")
	Optional<QuestionEntity> findById(@Param("questionId") Long questionId);
}
