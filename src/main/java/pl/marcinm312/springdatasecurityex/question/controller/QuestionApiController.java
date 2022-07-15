package pl.marcinm312.springdatasecurityex.question.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.question.service.QuestionManager;
import pl.marcinm312.springdatasecurityex.shared.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.shared.filter.Filter;
import pl.marcinm312.springdatasecurityex.shared.filter.SortField;
import pl.marcinm312.springdatasecurityex.shared.model.ListPage;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/questions")
public class QuestionApiController {

	private final QuestionManager questionManager;
	private final UserManager userManager;

	@Autowired
	public QuestionApiController(QuestionManager questionManager, UserManager userManager) {
		this.questionManager = questionManager;
		this.userManager = userManager;
	}

	@GetMapping
	public ListPage<QuestionGet> getQuestions(@RequestParam(required = false) String keyword,
											  @RequestParam(required = false) Integer pageNo,
											  @RequestParam(required = false) Integer pageSize,
											  @RequestParam(required = false) SortField sortField,
											  @RequestParam(required = false) Sort.Direction sortDirection) {

		sortField = Filter.checkQuestionsSortField(sortField);
		Filter filter = new Filter(keyword, pageNo, pageSize, sortField, sortDirection);
		return questionManager.searchPaginatedQuestions(filter);
	}

	@GetMapping("/{questionId}")
	public QuestionGet getQuestion(@PathVariable Long questionId) {
		return questionManager.getQuestion(questionId);
	}

	@PostMapping
	public QuestionGet createQuestion(@Valid @RequestBody QuestionCreateUpdate question, Authentication authentication) {
		UserEntity user = userManager.getUserByAuthentication(authentication);
		return questionManager.createQuestion(question, user);
	}

	@PutMapping("/{questionId}")
	public QuestionGet updateQuestion(@PathVariable Long questionId, @Valid @RequestBody QuestionCreateUpdate questionRequest,
								   Authentication authentication) {
		UserEntity user = userManager.getUserByAuthentication(authentication);
		return questionManager.updateQuestion(questionId, questionRequest, user);
	}

	@DeleteMapping("/{questionId}")
	public boolean deleteQuestion(@PathVariable Long questionId, Authentication authentication) {
		UserEntity user = userManager.getUserByAuthentication(authentication);
		return questionManager.deleteQuestion(questionId, user);
	}

	@GetMapping("/file-export")
	public ResponseEntity<Object> downloadFile(@RequestParam FileTypes fileType,
											   @RequestParam(required = false) String keyword,
											   @RequestParam(required = false) SortField sortField,
											   @RequestParam(required = false) Sort.Direction sortDirection) {

		sortField = Filter.checkQuestionsSortField(sortField);
		Filter filter = new Filter(keyword, sortField, sortDirection);
		return questionManager.generateQuestionsFile(fileType, filter);
	}
}
