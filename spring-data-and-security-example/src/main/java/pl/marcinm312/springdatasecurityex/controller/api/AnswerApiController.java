package pl.marcinm312.springdatasecurityex.controller.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.marcinm312.springdatasecurityex.model.Answer;
import pl.marcinm312.springdatasecurityex.service.db.AnswerManager;

@RestController
@RequestMapping("/api/questions/{questionId}/answers")
public class AnswerApiController {

	private AnswerManager answerManager;

	@Autowired
	public AnswerApiController(AnswerManager answerManager) {
		this.answerManager = answerManager;
	}

	@GetMapping
	public List<Answer> getAnswersByQuestionId(@PathVariable Long questionId) {
		return answerManager.getAnswersByQuestionId(questionId);
	}

	@GetMapping("/{answerId}")
	public Answer getAnswerByQuestionIdAndAnswerId(@PathVariable Long questionId, @PathVariable Long answerId) {
		return answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
	}

	@PostMapping
	public Answer addAnswer(@PathVariable Long questionId, @Valid @RequestBody Answer answer) {
		return answerManager.addAnswer(questionId, answer);
	}

	@PutMapping("/{answerId}")
	public Answer updateAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
			@Valid @RequestBody Answer answerRequest) {
		return answerManager.updateAnswer(questionId, answerId, answerRequest);
	}

	@DeleteMapping("/{answerId}")
	public void deleteAnswer(@PathVariable Long questionId, @PathVariable Long answerId) {
		answerManager.deleteAnswer(questionId, answerId);
	}
}
