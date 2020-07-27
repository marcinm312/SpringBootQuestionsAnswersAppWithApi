package pl.marcinm312.springdatasecurityex.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.model.Answer;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.service.db.AnswerManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/questions/{questionId}/answers")
public class AnswerApiController {

	private final AnswerManager answerManager;
	private final UserManager userManager;

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
	public boolean deleteAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
			Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		return answerManager.deleteAnswer(questionId, answerId, user);
	}
}
