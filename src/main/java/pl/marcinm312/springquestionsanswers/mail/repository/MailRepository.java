package pl.marcinm312.springquestionsanswers.mail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.marcinm312.springquestionsanswers.mail.model.MailEntity;

@Repository
public interface MailRepository extends JpaRepository<MailEntity, Long> {

}
