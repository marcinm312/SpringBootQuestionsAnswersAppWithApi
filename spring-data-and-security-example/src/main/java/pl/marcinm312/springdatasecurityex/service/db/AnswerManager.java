package pl.marcinm312.springdatasecurityex.service.db;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.marcinm312.springdatasecurityex.enums.Roles;
import pl.marcinm312.springdatasecurityex.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.Answer;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.repository.AnswerRepository;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;

@Service
public class AnswerManager {

	private AnswerRepository answerRepository;
	private QuestionRepository questionRepository;

	@Autowired
	public AnswerManager(AnswerRepository answerRepository, QuestionRepository questionRepository) {
		this.answerRepository = answerRepository;
		this.questionRepository = questionRepository;
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
			return answerRepository.save(answer);
		}).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
	}

	public Answer updateAnswer(Long questionId, Long answerId, Answer answerRequest, User user) {
		if (!questionRepository.existsById(questionId)) {
			throw new ResourceNotFoundException("Question not found with id " + questionId);
		}
		return answerRepository.findById(answerId).map(answer -> {
			if (answer.getUser().getId() == user.getId() || user.getRole().equals(Roles.ROLE_ADMIN.name())) {
				answer.setText(answerRequest.getText());
				return answerRepository.save(answer);
			} else {
				throw new ChangeNotAllowedException("Change not allowed!");
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
				throw new ChangeNotAllowedException("Change not allowed!");
			}
		}).orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));
	}
}
