package pl.marcinm312.springquestionsanswers.answer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springquestionsanswers.answer.model.dto.AnswerCreateUpdate;
import pl.marcinm312.springquestionsanswers.answer.model.dto.AnswerGet;
import pl.marcinm312.springquestionsanswers.answer.service.AnswerManager;
import pl.marcinm312.springquestionsanswers.question.model.dto.QuestionGet;
import pl.marcinm312.springquestionsanswers.question.service.QuestionManager;
import pl.marcinm312.springquestionsanswers.shared.enums.FileType;
import pl.marcinm312.springquestionsanswers.shared.exception.ChangeNotAllowedException;
import pl.marcinm312.springquestionsanswers.shared.exception.FileException;
import pl.marcinm312.springquestionsanswers.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springquestionsanswers.shared.filter.Filter;
import pl.marcinm312.springquestionsanswers.shared.filter.SortField;
import pl.marcinm312.springquestionsanswers.shared.model.ListPage;
import pl.marcinm312.springquestionsanswers.shared.utils.ControllerUtils;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;
import pl.marcinm312.springquestionsanswers.user.service.UserManager;

import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/app/questions/{questionId}/answers")
public class AnswerWebController {

	private static final String USER_LOGIN = "userLogin";
	private static final String QUESTION = "question";
	private static final String ANSWER = "answer";
	private static final String ANSWERS_VIEW = "answers";
	private static final String CREATE_ANSWER_VIEW = "createAnswer";
	private static final String OLD_ANSWER = "oldAnswer";
	private static final String EDIT_ANSWER_VIEW = "editAnswer";
	private static final String DELETE_ANSWER_VIEW = "deleteAnswer";

	private final QuestionManager questionManager;
	private final AnswerManager answerManager;
	private final UserManager userManager;


	@GetMapping
	public String answersGet(Model model, @PathVariable Long questionId, Authentication authentication,
							 HttpServletResponse response,
							 @RequestParam(required = false) String keyword,
							 @RequestParam(required = false) Integer pageNo,
							 @RequestParam(required = false) Integer pageSize,
							 @RequestParam(required = false) SortField sortField,
							 @RequestParam(required = false) Sort.Direction sortDirection) {

		log.info("Loading answers page for question.id = {}", questionId);
		String userName = authentication.getName();
		sortField = Filter.checkAnswersSortField(sortField);
		Filter filter = new Filter(keyword, pageNo, pageSize, sortField, sortDirection);
		String sortDir = filter.getSortDirection().name().toUpperCase();
		ListPage<AnswerGet> paginatedAnswers;
		QuestionGet question;
		try {
			paginatedAnswers = answerManager.searchPaginatedAnswers(questionId, filter);
			log.info("Answers list size: {}", paginatedAnswers.itemsList().size());
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			return ControllerUtils.getResourceNotFoundView(model, userName, e, response);
		}
		model.addAttribute("questionId", questionId);
		model.addAttribute("answerList", paginatedAnswers.itemsList());
		model.addAttribute("filter", filter);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", "ASC".equals(sortDir) ? "DESC" : "ASC");
		model.addAttribute("totalPages", paginatedAnswers.totalPages());
		model.addAttribute("totalItems", paginatedAnswers.totalElements());
		model.addAttribute(QUESTION, question);
		model.addAttribute(USER_LOGIN, userName);
		return ANSWERS_VIEW;
	}

	@PostMapping("/new")
	public String createAnswer(@ModelAttribute("answer") @Validated AnswerCreateUpdate answer, BindingResult bindingResult,
							   Model model, @PathVariable Long questionId, Authentication authentication,
							   HttpServletResponse response) {

		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			QuestionGet question = questionManager.getQuestion(questionId);
			model.addAttribute(QUESTION, question);
			model.addAttribute(ANSWER, answer);
			model.addAttribute(USER_LOGIN, userName);
			return CREATE_ANSWER_VIEW;
		}
		UserEntity user = userManager.getUserFromAuthentication(authentication);
		try {
			answerManager.addAnswer(questionId, answer, user);
		} catch (ResourceNotFoundException e) {
			return ControllerUtils.getResourceNotFoundView(model, userName, e, response);
		}
		return "redirect:..";
	}

	@GetMapping("/new")
	public String createAnswerView(Model model, @PathVariable Long questionId, Authentication authentication,
								   HttpServletResponse response) {

		String userName = authentication.getName();
		QuestionGet question;
		try {
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			return ControllerUtils.getResourceNotFoundView(model, userName, e, response);
		}
		model.addAttribute(QUESTION, question);
		model.addAttribute(ANSWER, new AnswerCreateUpdate());
		model.addAttribute(USER_LOGIN, userName);
		return CREATE_ANSWER_VIEW;
	}

	@PostMapping("/{answerId}/edit")
	public String editAnswer(@ModelAttribute("answer") @Validated AnswerCreateUpdate answer, BindingResult bindingResult,
							 Model model, @PathVariable Long questionId, @PathVariable Long answerId,
							 Authentication authentication, HttpServletResponse response) {

		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			AnswerGet oldAnswer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
			QuestionGet question = questionManager.getQuestion(questionId);
			model.addAttribute(QUESTION, question);
			model.addAttribute(OLD_ANSWER, oldAnswer);
			model.addAttribute(ANSWER, answer);
			model.addAttribute(USER_LOGIN, userName);
			return EDIT_ANSWER_VIEW;
		}
		UserEntity user = userManager.getUserFromAuthentication(authentication);
		try {
			answerManager.updateAnswer(questionId, answerId, answer, user);
		} catch (ChangeNotAllowedException e) {
			return ControllerUtils.getChangeNotAllowedView(model, userName, e, response);
		} catch (ResourceNotFoundException e) {
			return ControllerUtils.getResourceNotFoundView(model, userName, e, response);
		}
		return "redirect:../..";
	}

	@GetMapping("/{answerId}/edit")
	public String editAnswerView(Model model, @PathVariable Long questionId, @PathVariable Long answerId,
								 Authentication authentication, HttpServletResponse response) {

		return getEditOrRemoveAnswerView(model, questionId, answerId, authentication, true, response);
	}

	@PostMapping("/{answerId}/delete")
	public String removeAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
							   Authentication authentication, Model model, HttpServletResponse response) {

		UserEntity user = userManager.getUserFromAuthentication(authentication);
		String userName = authentication.getName();
		try {
			answerManager.deleteAnswer(questionId, answerId, user);
		} catch (ChangeNotAllowedException e) {
			return ControllerUtils.getChangeNotAllowedView(model, userName, e, response);
		} catch (ResourceNotFoundException e) {
			return ControllerUtils.getResourceNotFoundView(model, userName, e, response);
		}
		return "redirect:../..";
	}

	@GetMapping("/{answerId}/delete")
	public String removeAnswerView(Model model, @PathVariable Long questionId, @PathVariable Long answerId,
								   Authentication authentication, HttpServletResponse response) {

		return getEditOrRemoveAnswerView(model, questionId, answerId, authentication, false, response);
	}

	@GetMapping("/file-export")
	public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long questionId,
														  @RequestParam FileType fileType,
														  @RequestParam(required = false) String keyword,
														  @RequestParam(required = false) SortField sortField,
														  @RequestParam(required = false) Sort.Direction sortDirection)
			throws ResourceNotFoundException, FileException {

		sortField = Filter.checkAnswersSortField(sortField);
		Filter filter = new Filter(keyword, sortField, sortDirection);
		return answerManager.generateAnswersFile(questionId, fileType, filter);
	}

	private String getEditOrRemoveAnswerView(Model model, Long questionId, Long answerId, Authentication authentication,
											 boolean isEdit, HttpServletResponse response) {
		String userName = authentication.getName();
		AnswerGet answer;
		QuestionGet question;
		try {
			answer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			return ControllerUtils.getResourceNotFoundView(model, userName, e, response);
		}
		model.addAttribute(ANSWER, answer);
		model.addAttribute(QUESTION, question);
		model.addAttribute(USER_LOGIN, userName);
		if (isEdit) {
			model.addAttribute(OLD_ANSWER, answer);
			return EDIT_ANSWER_VIEW;
		}
		return DELETE_ANSWER_VIEW;
	}
}
