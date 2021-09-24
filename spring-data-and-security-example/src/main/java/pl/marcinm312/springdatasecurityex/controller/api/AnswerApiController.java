package pl.marcinm312.springdatasecurityex.controller.api;

import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.answer.dto.AnswerCreateUpdate;
import pl.marcinm312.springdatasecurityex.model.answer.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.service.db.AnswerManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;

import javax.validation.Valid;
import java.io.IOException;
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
	public ResponseEntity<Object> downloadPdf(@PathVariable Long questionId)
			throws IOException, DocumentException, ResourceNotFoundException {
		return answerManager.generateAnswersFile(questionId, FileTypes.PDF);
	}

	@GetMapping("/excel-export")
	public ResponseEntity<Object> downloadExcel(@PathVariable Long questionId)
			throws IOException, ResourceNotFoundException, DocumentException {
		return answerManager.generateAnswersFile(questionId, FileTypes.EXCEL);
	}
}
