package pl.marcinm312.springdatasecurityex.service.db;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.Answer;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.AnswerRepository;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.service.MailService;

import javax.mail.MessagingException;
import java.util.List;

@Service
public class AnswerManager {

	private final AnswerRepository answerRepository;
	private final QuestionRepository questionRepository;
	private final MailService mailService;

	protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public AnswerManager(AnswerRepository answerRepository, QuestionRepository questionRepository,
						 MailService mailService) {
		this.answerRepository = answerRepository;
		this.questionRepository = questionRepository;
		this.mailService = mailService;
	}

	public List<Answer> getAnswersByQuestionId(Long questionId) {
		checkIfQuestionExistsByQuestionId(questionId);
		return answerRepository.findByQuestionIdOrderByIdDesc(questionId);
	}

	public Answer getAnswerByQuestionIdAndAnswerId(Long questionId, Long answerId) {
		checkIfQuestionExistsByQuestionId(questionId);
		return answerRepository.findById(answerId)
				.orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));
	}

	public Answer addAnswer(Long questionId, Answer answer, User user) {
		return questionRepository.findById(questionId).map(question -> {
			answer.setQuestion(question);
			answer.setUser(user);
			log.info("Adding answer = " + answer.toString());
			Answer savedAnswer = answerRepository.save(answer);
			try {
				String email = question.getUser().getEmail();
				String subject = "Opublikowano odpowiedź na Twoje pytanie o id: " + question.getId();
				String content = generateEmailContent(question, savedAnswer, true);
				mailService.sendMail(email, subject, content, true);
			} catch (MessagingException e) {
				log.error("An error occurred while sending the email");
				e.printStackTrace();
			}
			return savedAnswer;

		}).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
	}

	public Answer updateAnswer(Long questionId, Long answerId, Answer answerRequest, User user) {
		log.info("Updating answer");
		checkIfQuestionExistsByQuestionId(questionId);
		return answerRepository.findById(answerId).map(answer -> {
			Long answerUserId = answer.getUser().getId();
			Long currentUserId = user.getId();
			String currentUserRole = user.getRole();
			log.info("answerUserId=" + answerUserId);
			log.info("currentUserId=" + currentUserId);
			log.info("currentUserRole=" + currentUserRole);
			if (answerUserId.equals(currentUserId) || currentUserRole.equals(Roles.ROLE_ADMIN.name())) {
				log.info("Permitted user");
				log.info("Old answer = " + answer.toString());
				answer.setText(answerRequest.getText());
				log.info("New answer = " + answer.toString());
				Answer savedAnswer = answerRepository.save(answer);
				try {
					Question question = savedAnswer.getQuestion();
					String email = question.getUser().getEmail();
					String subject = "Zaktualizowano odpowiedź na Twoje pytanie o id: " + question.getId();
					String content = generateEmailContent(question, savedAnswer, false);
					mailService.sendMail(email, subject, content, true);
				} catch (MessagingException e) {
					log.error("An error occurred while sending the email");
					e.printStackTrace();
				}
				return savedAnswer;
			} else {
				log.info("User is not permitted");
				throw new ChangeNotAllowedException();
			}
		}).orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));
	}

	public boolean deleteAnswer(Long questionId, Long answerId, User user) {
		log.info("Deleting answer.id = " + answerId);
		checkIfQuestionExistsByQuestionId(questionId);
		return answerRepository.findById(answerId).map(answer -> {
			Long answerUserId = answer.getUser().getId();
			Long currentUserId = user.getId();
			String currentUserRole = user.getRole();
			log.info("answerUserId=" + answerUserId);
			log.info("currentUserId=" + currentUserId);
			log.info("currentUserRole=" + currentUserRole);
			if (answerUserId.equals(currentUserId) || currentUserRole.equals(Roles.ROLE_ADMIN.name())) {
				log.info("Permitted user");
				answerRepository.delete(answer);
				return true;
			} else {
				log.info("User is not permitted");
				throw new ChangeNotAllowedException();
			}
		}).orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));
	}

	private void checkIfQuestionExistsByQuestionId(Long questionId) {
		if (!questionRepository.existsById(questionId)) {
			throw new ResourceNotFoundException("Question not found with id " + questionId);
		}
	}

	private String generateEmailContent(Question question, Answer answer, boolean isNewAnswer) {
		User questionUser = question.getUser();
		User answerUser = answer.getUser();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Witaj ").append(questionUser.getUsername()).append(",");
		if (isNewAnswer) {
			stringBuilder.append("<br/><br/>Użytkownik <b>").append(answerUser.getUsername())
					.append("</b> opublikował odpowiedź na Twoje pytanie:");
		} else {
			stringBuilder.append("<br/><br/>Użytkownik <b>").append(answerUser.getUsername())
					.append("</b> zaktualizował odpowiedź na Twoje pytanie:");
		}
		stringBuilder.append("<br/><br/><b>Tytuł:</b><br/>").append(question.getTitle());
		stringBuilder.append("<br/><br/><b>Opis:</b><br/>").append(question.getDescription());
		stringBuilder.append("<br/><br/><br/><b>Treść odpowiedzi:</b><br/>")
				.append(answer.getText().replace("\n", "<br/>"));
		return stringBuilder.toString();
	}
}
