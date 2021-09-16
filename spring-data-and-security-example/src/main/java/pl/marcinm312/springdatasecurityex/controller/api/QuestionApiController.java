package pl.marcinm312.springdatasecurityex.controller.api;

import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.FileResponseGenerator;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

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
	public List<QuestionGet> getQuestions() {
		return questionManager.getQuestions();
	}

	@GetMapping("/{questionId}")
	public QuestionGet getQuestion(@PathVariable Long questionId) {
		return questionManager.getQuestion(questionId);
	}

	@PostMapping
	public QuestionGet createQuestion(@Valid @RequestBody QuestionCreateUpdate question, Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		return questionManager.createQuestion(question, user);
	}

	@PutMapping("/{questionId}")
	public QuestionGet updateQuestion(@PathVariable Long questionId, @Valid @RequestBody QuestionCreateUpdate questionRequest,
								   Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		return questionManager.updateQuestion(questionId, questionRequest, user);
	}

	@DeleteMapping("/{questionId}")
	public boolean deleteQuestion(@PathVariable Long questionId, Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		return questionManager.deleteQuestion(questionId, user);
	}

	@GetMapping("/pdf-export")
	public ResponseEntity<Object> downloadPdf() throws IOException, DocumentException {
		List<QuestionGet> questionsList = questionManager.getQuestions();
		return FileResponseGenerator.generateQuestionsFile(questionsList, FileTypes.PDF);
	}

	@GetMapping("/excel-export")
	public ResponseEntity<Object> downloadExcel() throws IOException, DocumentException {
		List<QuestionGet> questionsList = questionManager.getQuestions();
		return FileResponseGenerator.generateQuestionsFile(questionsList, FileTypes.EXCEL);
	}
}
