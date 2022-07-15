package pl.marcinm312.springdatasecurityex.answer.controller;

import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerCreateUpdate;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.answer.service.AnswerManager;
import pl.marcinm312.springdatasecurityex.shared.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.shared.filter.Filter;
import pl.marcinm312.springdatasecurityex.shared.filter.SortField;
import pl.marcinm312.springdatasecurityex.shared.model.ListPage;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;

import javax.validation.Valid;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions/{questionId}/answers")
public class AnswerApiController {

	private final AnswerManager answerManager;
	private final UserManager userManager;


	@GetMapping
	public ListPage<AnswerGet> getAnswers(@PathVariable Long questionId,
										  @RequestParam(required = false) String keyword,
										  @RequestParam(required = false) Integer pageNo,
										  @RequestParam(required = false) Integer pageSize,
										  @RequestParam(required = false) SortField sortField,
										  @RequestParam(required = false) Sort.Direction sortDirection) {

		sortField = Filter.checkAnswersSortField(sortField);
		Filter filter = new Filter(keyword, pageNo, pageSize, sortField, sortDirection);
		return answerManager.searchPaginatedAnswers(questionId, filter);
	}

	@GetMapping("/{answerId}")
	public AnswerGet getAnswerByQuestionIdAndAnswerId(@PathVariable Long questionId, @PathVariable Long answerId) {
		return answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
	}

	@PostMapping
	public AnswerGet addAnswer(@PathVariable Long questionId, @Valid @RequestBody AnswerCreateUpdate answer,
							   Authentication authentication) {
		UserEntity user = userManager.getUserByAuthentication(authentication);
		return answerManager.addAnswer(questionId, answer, user);
	}

	@PutMapping("/{answerId}")
	public AnswerGet updateAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
								  @Valid @RequestBody AnswerCreateUpdate answerRequest, Authentication authentication) {
		UserEntity user = userManager.getUserByAuthentication(authentication);
		return answerManager.updateAnswer(questionId, answerId, answerRequest, user);
	}

	@DeleteMapping("/{answerId}")
	public boolean deleteAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
								Authentication authentication) {
		UserEntity user = userManager.getUserByAuthentication(authentication);
		return answerManager.deleteAnswer(questionId, answerId, user);
	}

	@GetMapping("/file-export")
	public ResponseEntity<Object> downloadFile(@PathVariable Long questionId,
											   @RequestParam FileTypes fileType,
											   @RequestParam(required = false) String keyword,
											   @RequestParam(required = false) SortField sortField,
											   @RequestParam(required = false) Sort.Direction sortDirection)
			throws ResourceNotFoundException {

		sortField = Filter.checkAnswersSortField(sortField);
		Filter filter = new Filter(keyword, sortField, sortDirection);
		return answerManager.generateAnswersFile(questionId, fileType, filter);
	}
}
