package pl.marcinm312.springdatasecurityex.service.db;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;

@Service
public class QuestionManager {

	private QuestionRepository questionRepository;

	@Autowired
	public QuestionManager(QuestionRepository questionRepository) {
		this.questionRepository = questionRepository;
	}

	public List<Question> getQuestions() {
		return questionRepository.findAll();
	}

	public Question getQuestion(Long questionId) {
		return questionRepository.findById(questionId).map(question -> {
			return question;
		}).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
	}

	public Question createQuestion(Question question) {
		return questionRepository.save(question);
	}

	public Question updateQuestion(Long questionId, Question questionRequest) {
		return questionRepository.findById(questionId).map(question -> {
			question.setTitle(questionRequest.getTitle());
			question.setDescription(questionRequest.getDescription());
			return questionRepository.save(question);

		}).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
	}

	public boolean deleteQuestion(Long questionId) {
		return questionRepository.findById(questionId).map(question -> {
			questionRepository.delete(question);
			return true;
		}).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
	}
}
