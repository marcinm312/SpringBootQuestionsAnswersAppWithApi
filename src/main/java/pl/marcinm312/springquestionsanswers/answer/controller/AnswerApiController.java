package pl.marcinm312.springquestionsanswers.answer.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springquestionsanswers.answer.model.dto.AnswerCreateUpdate;
import pl.marcinm312.springquestionsanswers.answer.model.dto.AnswerGet;
import pl.marcinm312.springquestionsanswers.answer.service.AnswerManager;
import pl.marcinm312.springquestionsanswers.shared.file.FileType;
import pl.marcinm312.springquestionsanswers.shared.exception.FileException;
import pl.marcinm312.springquestionsanswers.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springquestionsanswers.shared.filter.Filter;
import pl.marcinm312.springquestionsanswers.shared.filter.SortField;
import pl.marcinm312.springquestionsanswers.shared.model.ListPage;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.service.UserManager;

import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions/{questionId}/answers")
public class AnswerApiController {

	private final AnswerManager answerManager;
	private final UserManager userManager;

	@GetMapping
	public ListPage<AnswerGet> getAnswers(@PathVariable Long questionId,
										  @RequestParam(required = false) String keyword,
										  @RequestParam(required = false) @Parameter(description = "Default value: `1`") Integer pageNo,
										  @RequestParam(required = false) @Parameter(description = "Default value: `5`") Integer pageSize,
										  @RequestParam(required = false) @Parameter(description = "Default value: `ID`") SortField sortField,
										  @RequestParam(required = false) @Parameter(description = "Default value: `DESC`") Sort.Direction sortDirection) {

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

		UserEntity user = userManager.getUserFromAuthentication(authentication);
		return answerManager.addAnswer(questionId, answer, user);
	}

	@PutMapping("/{answerId}")
	public AnswerGet updateAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
								  @Valid @RequestBody AnswerCreateUpdate answerRequest, Authentication authentication) {

		UserEntity user = userManager.getUserFromAuthentication(authentication);
		return answerManager.updateAnswer(questionId, answerId, answerRequest, user);
	}

	@DeleteMapping("/{answerId}")
	public boolean deleteAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
								Authentication authentication) {

		UserEntity user = userManager.getUserFromAuthentication(authentication);
		return answerManager.deleteAnswer(questionId, answerId, user);
	}

	@GetMapping("/file-export")
	public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long questionId,
														  @RequestParam FileType fileType,
														  @RequestParam(required = false) String keyword,
														  @RequestParam(required = false) @Parameter(description = "Default value: `1`") Integer pageNo,
														  @RequestParam(required = false) @Parameter(description = "Default value: `5`") Integer pageSize,
														  @RequestParam(required = false) @Parameter(description = "Default value: `ID`") SortField sortField,
														  @RequestParam(required = false) @Parameter(description = "Default value: `DESC`") Sort.Direction sortDirection)
			throws ResourceNotFoundException, FileException {

		sortField = Filter.checkAnswersSortField(sortField);
		Filter filter = new Filter(keyword, pageNo, pageSize, sortField, sortDirection);
		return answerManager.generateAnswersFile(questionId, fileType, filter);
	}
}
