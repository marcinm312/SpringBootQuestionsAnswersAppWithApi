package pl.marcinm312.springdatasecurityex.service.db;

import java.util.List;

import javax.mail.MessagingException;

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

@Service
public class AnswerManager {

	private AnswerRepository answerRepository;
	private QuestionRepository questionRepository;
	private MailService mailService;

	@Autowired
	public AnswerManager(AnswerRepository answerRepository, QuestionRepository questionRepository,
			MailService mailService) {
		this.answerRepository = answerRepository;
		this.questionRepository = questionRepository;
		this.mailService = mailService;
	}

	public List<Answer> getAnswersByQuestionId(Long questionId) {
		return answerRepository.findByQuestionId(questionId);
	}

	public Answer getAnswerByQuestionIdAndAnswerId(Long questionId, Long answerId) {
		if (!questionRepository.existsById(questionId)) {
			throw new ResourceNotFoundException("Question not found with id " + questionId);
		}
		return answerRepository.findById(answerId).map(answer -> {
			return answer;
		}).orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));
	}

	public Answer addAnswer(Long questionId, Answer answer, User user) {
		return questionRepository.findById(questionId).map(question -> {
			answer.setQuestion(question);
			answer.setUser(user);
			Answer savedAnswer = answerRepository.save(answer);
			try {
				String email = question.getUser().getEmail();
				String subject = "Opublikowano odpowiedź na Twoje pytanie o id: " + question.getId();
				String content = generateEmailContent(question, savedAnswer, true);
				mailService.sendMail(email, subject, content, true);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			return savedAnswer;

		}).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
	}

	public Answer updateAnswer(Long questionId, Long answerId, Answer answerRequest, User user) {
		if (!questionRepository.existsById(questionId)) {
			throw new ResourceNotFoundException("Question not found with id " + questionId);
		}
		return answerRepository.findById(answerId).map(answer -> {
			if (answer.getUser().getId() == user.getId() || user.getRole().equals(Roles.ROLE_ADMIN.name())) {
				answer.setText(answerRequest.getText());
				Answer savedAnswer = answerRepository.save(answer);
				try {
					Question question = savedAnswer.getQuestion();
					String email = question.getUser().getEmail();
					String subject = "Zaktualizowano odpowiedź na Twoje pytanie o id: " + question.getId();
					String content = generateEmailContent(question, savedAnswer, false);
					mailService.sendMail(email, subject, content, true);
				} catch (MessagingException e) {
					e.printStackTrace();
				}
				return savedAnswer;
			} else {
				throw new ChangeNotAllowedException();
			}
		}).orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));
	}

	public boolean deleteAnswer(Long questionId, Long answerId, User user) {
		if (!questionRepository.existsById(questionId)) {
			throw new ResourceNotFoundException("Question not found with id " + questionId);
		}
		return answerRepository.findById(answerId).map(answer -> {
			if (answer.getUser().getId() == user.getId() || user.getRole().equals(Roles.ROLE_ADMIN.name())) {
				answerRepository.delete(answer);
				return true;
			} else {
				throw new ChangeNotAllowedException();
			}
		}).orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));
	}

	private String generateEmailContent(Question question, Answer answer, boolean isNewAnswer) {
		User questionUser = question.getUser();
		User answerUser = answer.getUser();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Witaj " + questionUser.getFirstName() + " " + questionUser.getLastName() + ",");
		if (isNewAnswer) {
			stringBuilder.append("<br/><br/>Użytkownik <b>" + answerUser.getUsername()
					+ "</b> opublikował odpowiedź na Twoje pytanie:");
		} else {
			stringBuilder.append("<br/><br/>Użytkownik <b>" + answerUser.getUsername()
					+ "</b> zaktualizował odpowiedź na Twoje pytanie:");
		}
		stringBuilder.append("<br/><br/><b>Tytuł:</b><br/>" + question.getTitle());
		stringBuilder.append("<br/><br/><b>Opis:</b><br/>" + question.getDescription());
		stringBuilder.append("<br/><br/><br/><b>Treść odpowiedzi:</b><br/>" + answer.getText().replace("\n", "<br/>"));
		return stringBuilder.toString();
	}
}
