package pl.marcinm312.springdatasecurityex.controller.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.marcinm312.springdatasecurityex.model.Answer;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.service.db.AnswerManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;

@RestController
@RequestMapping("/api/questions/{questionId}/answers")
public class AnswerApiController {

	private AnswerManager answerManager;
	private UserManager userManager;

	@Autowired
	public AnswerApiController(AnswerManager answerManager, UserManager userManager) {
		this.answerManager = answerManager;
		this.userManager = userManager;
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
	public Answer addAnswer(@PathVariable Long questionId, @Valid @RequestBody Answer answer,
			Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		return answerManager.addAnswer(questionId, answer, user);
	}

	@PutMapping("/{answerId}")
	public Answer updateAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
			@Valid @RequestBody Answer answerRequest, Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		return answerManager.updateAnswer(questionId, answerId, answerRequest, user);
	}

	@DeleteMapping("/{answerId}")
	public void deleteAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
			Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		answerManager.deleteAnswer(questionId, answerId, user);
	}
}
