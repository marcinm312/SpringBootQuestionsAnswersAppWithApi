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

import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;

@RestController
@RequestMapping("/api/questions")
public class QuestionApiController {

	private QuestionManager questionManager;
	private UserManager userManager;

	@Autowired
	public QuestionApiController(QuestionManager questionManager, UserManager userManager) {
		this.questionManager = questionManager;
		this.userManager = userManager;
	}

	@GetMapping
	public List<Question> getQuestions() {
		return questionManager.getQuestions();
	}

	@GetMapping("/{questionId}")
	public Question getQuestion(@PathVariable Long questionId) {
		return questionManager.getQuestion(questionId);
	}

	@PostMapping
	public Question createQuestion(@Valid @RequestBody Question question, Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		return questionManager.createQuestion(question, user);
	}

	@PutMapping("/{questionId}")
	public Question updateQuestion(@PathVariable Long questionId, @Valid @RequestBody Question questionRequest,
			Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		return questionManager.updateQuestion(questionId, questionRequest, user);
	}

	@DeleteMapping("/{questionId}")
	public void deleteQuestion(@PathVariable Long questionId, Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		questionManager.deleteQuestion(questionId, user);
	}
}
