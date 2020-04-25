package pl.marcinm312.springdatasecurityex.service.db;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.Answer;
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

	public Answer addAnswer(Long questionId, Answer answer) {
		return questionRepository.findById(questionId).map(question -> {
			answer.setQuestion(question);
			return answerRepository.save(answer);
		}).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
	}

	public Answer updateAnswer(Long questionId, Long answerId, Answer answerRequest) {
		if (!questionRepository.existsById(questionId)) {
			throw new ResourceNotFoundException("Question not found with id " + questionId);
		}
		return answerRepository.findById(answerId).map(answer -> {
			answer.setText(answerRequest.getText());
			return answerRepository.save(answer);
		}).orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));
	}

	public boolean deleteAnswer(Long questionId, Long answerId) {
		if (!questionRepository.existsById(questionId)) {
			throw new ResourceNotFoundException("Question not found with id " + questionId);
		}
		return answerRepository.findById(answerId).map(answer -> {
			answerRepository.delete(answer);
			return true;
		}).orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));
	}
}
