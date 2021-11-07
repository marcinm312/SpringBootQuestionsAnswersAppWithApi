package pl.marcinm312.springdatasecurityex.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

	@Query("SELECT q FROM QuestionEntity q LEFT JOIN FETCH q.user ORDER BY q.id DESC")
	List<QuestionEntity> findAllByOrderByIdDesc();
}