package pl.marcinm312.springdatasecurityex.controller.api;

import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.Answer;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.service.db.AnswerManager;
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
@RequestMapping("/api/questions/{questionId}/answers")
public class AnswerApiController {

	private final QuestionManager questionManager;
	private final AnswerManager answerManager;
	private final PdfGenerator pdfGenerator;
	private final ExcelGenerator excelGenerator;
	private final UserManager userManager;

	@Autowired
	public AnswerApiController(QuestionManager questionManager, AnswerManager answerManager, PdfGenerator pdfGenerator, ExcelGenerator excelGenerator, UserManager userManager) {
		this.questionManager = questionManager;
		this.answerManager = answerManager;
		this.pdfGenerator = pdfGenerator;
		this.excelGenerator = excelGenerator;
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

	@GetMapping("/pdf-export")
	public ResponseEntity<?> downloadPdf(@PathVariable Long questionId) throws IOException, DocumentException {
		Question question;
		List<Answer> answersList;
		try {
			question = questionManager.getQuestion(questionId);
			answersList = answerManager.getAnswersByQuestionId(questionId);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
		File file = pdfGenerator.generateAnswersPdfFile(answersList, question);
		return FileResponseGenerator.generateResponseWithFile(file);
	}

	@GetMapping("/excel-export")
	public ResponseEntity<?> downloadExcel(@PathVariable Long questionId) throws IOException {
		Question question;
		List<Answer> answersList;
		try {
			question = questionManager.getQuestion(questionId);
			answersList = answerManager.getAnswersByQuestionId(questionId);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
		File file = excelGenerator.generateAnswersExcelFile(answersList, question);
		return FileResponseGenerator.generateResponseWithFile(file);
	}
}
