package pl.marcinm312.springdatasecurityex.question.controller;

import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.question.enums.QuestionSortField;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.question.service.QuestionManager;
import pl.marcinm312.springdatasecurityex.shared.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.shared.model.ListPage;
import pl.marcinm312.springdatasecurityex.shared.pagination.Filter;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;

import javax.validation.Valid;
import java.io.IOException;

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
											  @RequestParam(required = false) QuestionSortField sortField,
											  @RequestParam(required = false) Sort.Direction sortDirection) {

		if (sortField == null) {
			sortField = QuestionSortField.ID;
		}
		Filter filter = new Filter(keyword, pageNo, pageSize, sortField.getField(), sortDirection);
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

	@GetMapping("/pdf-export")
	public ResponseEntity<Object> downloadPdf() throws IOException, DocumentException {
		return questionManager.generateQuestionsFile(FileTypes.PDF);
	}

	@GetMapping("/excel-export")
	public ResponseEntity<Object> downloadExcel() throws IOException, DocumentException {
		return questionManager.generateQuestionsFile(FileTypes.EXCEL);
	}
}
