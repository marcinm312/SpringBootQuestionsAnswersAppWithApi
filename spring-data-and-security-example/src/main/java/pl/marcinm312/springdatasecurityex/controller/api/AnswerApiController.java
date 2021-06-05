package pl.marcinm312.springdatasecurityex.controller.api;

import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.answer.dto.AnswerCreateUpdate;
import pl.marcinm312.springdatasecurityex.model.answer.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.model.user.User;
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
	public AnswerApiController(QuestionManager questionManager, AnswerManager answerManager, PdfGenerator pdfGenerator,
							   ExcelGenerator excelGenerator, UserManager userManager) {
		this.questionManager = questionManager;
		this.answerManager = answerManager;
		this.pdfGenerator = pdfGenerator;
		this.excelGenerator = excelGenerator;
		this.userManager = userManager;
	}

	@GetMapping
	public List<AnswerGet> getAnswersByQuestionId(@PathVariable Long questionId) {
		return answerManager.getAnswersByQuestionId(questionId);
	}

	@GetMapping("/{answerId}")
	public AnswerGet getAnswerByQuestionIdAndAnswerId(@PathVariable Long questionId, @PathVariable Long answerId) {
		return answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
	}

	@PostMapping
	public AnswerGet addAnswer(@PathVariable Long questionId, @Valid @RequestBody AnswerCreateUpdate answer,
							Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		return answerManager.addAnswer(questionId, answer, user);
	}

	@PutMapping("/{answerId}")
	public AnswerGet updateAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
							   @Valid @RequestBody AnswerCreateUpdate answerRequest, Authentication authentication) {
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
	public ResponseEntity<ByteArrayResource> downloadPdf(@PathVariable Long questionId)
			throws IOException, DocumentException, ResourceNotFoundException {
		QuestionGet question = questionManager.getQuestion(questionId);
		List<AnswerGet> answersList = answerManager.getAnswersByQuestionId(questionId);
		File file = pdfGenerator.generateAnswersPdfFile(answersList, question);
		return FileResponseGenerator.generateResponseWithFile(file);
	}

	@GetMapping("/excel-export")
	public ResponseEntity<ByteArrayResource> downloadExcel(@PathVariable Long questionId)
			throws IOException, ResourceNotFoundException {
		QuestionGet question = questionManager.getQuestion(questionId);
		List<AnswerGet> answersList = answerManager.getAnswersByQuestionId(questionId);
		File file = excelGenerator.generateAnswersExcelFile(answersList, question);
		return FileResponseGenerator.generateResponseWithFile(file);
	}
}
