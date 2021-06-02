package pl.marcinm312.springdatasecurityex.controller.api;

import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.model.question.Question;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.FileResponseGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionApiController {

	private final QuestionManager questionManager;
	private final PdfGenerator pdfGenerator;
	private final ExcelGenerator excelGenerator;
	private final UserManager userManager;

	@Autowired
	public QuestionApiController(QuestionManager questionManager, PdfGenerator pdfGenerator, ExcelGenerator excelGenerator, UserManager userManager) {
		this.questionManager = questionManager;
		this.pdfGenerator = pdfGenerator;
		this.excelGenerator = excelGenerator;
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
	public boolean deleteQuestion(@PathVariable Long questionId, Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		return questionManager.deleteQuestion(questionId, user);
	}

	@GetMapping("/pdf-export")
	public ResponseEntity<ByteArrayResource> downloadPdf() throws IOException, DocumentException {
		List<QuestionGet> questionsList = questionManager.getQuestions();
		File file = pdfGenerator.generateQuestionsPdfFile(questionsList);
		return FileResponseGenerator.generateResponseWithFile(file);
	}

	@GetMapping("/excel-export")
	public ResponseEntity<ByteArrayResource> downloadExcel() throws IOException {
		List<QuestionGet> questionsList = questionManager.getQuestions();
		File file = excelGenerator.generateQuestionsExcelFile(questionsList);
		return FileResponseGenerator.generateResponseWithFile(file);
	}
}
