package pl.marcinm312.springdatasecurityex.service.db;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;

@Service
public class QuestionManager {

	private final QuestionRepository questionRepository;

	protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public QuestionManager(QuestionRepository questionRepository) {
		this.questionRepository = questionRepository;
	}

	public List<Question> getQuestions() {
		return questionRepository.findAll();
	}

	public Question getQuestion(Long questionId) {
		return questionRepository.findById(questionId)
				.orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
	}

	public Question createQuestion(Question question, User user) {
		question.setUser(user);
		log.info("Creating question = " + question.toString());
		return questionRepository.save(question);
	}

	public Question updateQuestion(Long questionId, Question questionRequest, User user) {
		log.info("Updating question");
		return questionRepository.findById(questionId).map(question -> {
			Long questionUserId = question.getUser().getId();
			Long currentUserId = user.getId();
			String currentUserRole = user.getRole();
			log.info("questionUserId=" + questionUserId);
			log.info("currentUserId=" + currentUserId);
			log.info("currentUserRole=" + currentUserRole);
			if (questionUserId.equals(currentUserId) || currentUserRole.equals(Roles.ROLE_ADMIN.name())) {
				log.info("Permitted user");
				question.setTitle(questionRequest.getTitle());
				question.setDescription(questionRequest.getDescription());
				log.info("Old question = " + question.toString());
				log.info("New question = " + questionRequest.toString());
				return questionRepository.save(question);
			} else {
				log.info("User is not permitted");
				throw new ChangeNotAllowedException();
			}
		}).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
	}

	public boolean deleteQuestion(Long questionId, User user) {
		log.info("Deleting question.id = " + questionId);
		return questionRepository.findById(questionId).map(question -> {
			Long questionUserId = question.getUser().getId();
			Long currentUserId = user.getId();
			String currentUserRole = user.getRole();
			log.info("questionUserId=" + questionUserId);
			log.info("currentUserId=" + currentUserId);
			log.info("currentUserRole=" + currentUserRole);
			if (questionUserId.equals(currentUserId) || currentUserRole.equals(Roles.ROLE_ADMIN.name())) {
				log.info("Permitted user");
				questionRepository.delete(question);
				return true;
			} else {
				log.info("User is not permitted");
				throw new ChangeNotAllowedException();
			}
		}).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
	}
}
