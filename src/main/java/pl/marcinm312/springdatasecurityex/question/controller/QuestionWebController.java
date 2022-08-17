package pl.marcinm312.springdatasecurityex.question.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.question.service.QuestionManager;
import pl.marcinm312.springdatasecurityex.shared.enums.FileType;
import pl.marcinm312.springdatasecurityex.shared.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.shared.filter.Filter;
import pl.marcinm312.springdatasecurityex.shared.filter.SortField;
import pl.marcinm312.springdatasecurityex.shared.model.ListPage;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;

import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/app/questions")
public class QuestionWebController {

	private static final String USER_LOGIN = "userLogin";
	private static final String QUESTIONS_VIEW = "questions";
	private static final String QUESTION = "question";
	private static final String CREATE_QUESTION_VIEW = "createQuestion";
	private static final String OLD_QUESTION = "oldQuestion";
	private static final String EDIT_QUESTION_VIEW = "editQuestion";
	private static final String CHANGE_NOT_ALLOWED_VIEW = "changeNotAllowed";
	private static final String MESSAGE = "message";
	private static final String RESOURCE_NOT_FOUND_VIEW = "resourceNotFound";
	private static final String DELETE_QUESTION_VIEW = "deleteQuestion";

	private final QuestionManager questionManager;
	private final UserManager userManager;


	@GetMapping
	public String questionsGet(Model model, Authentication authentication,
							   @RequestParam(required = false) String keyword,
							   @RequestParam(required = false) Integer pageNo,
							   @RequestParam(required = false) Integer pageSize,
							   @RequestParam(required = false) SortField sortField,
							   @RequestParam(required = false) Sort.Direction sortDirection) {

		log.info("Loading questions page");
		String userName = authentication.getName();
		sortField = Filter.checkQuestionsSortField(sortField);
		Filter filter = new Filter(keyword, pageNo, pageSize, sortField, sortDirection);
		ListPage<QuestionGet> paginatedQuestions = questionManager.searchPaginatedQuestions(filter);
		log.info("Questions list size: {}", paginatedQuestions.getItemsList().size());
		String sortDir = filter.getSortDirection().name().toUpperCase();

		model.addAttribute("questionList", paginatedQuestions.getItemsList());
		model.addAttribute("filter", filter);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", "ASC".equals(sortDir) ? "DESC" : "ASC");
		model.addAttribute("totalPages", paginatedQuestions.getTotalPages());
		model.addAttribute("totalItems", paginatedQuestions.getTotalElements());
		model.addAttribute(USER_LOGIN, userName);

		return QUESTIONS_VIEW;
	}

	@PostMapping("/new")
	public String createQuestion(@ModelAttribute("question") @Validated QuestionCreateUpdate question, BindingResult bindingResult,
								 Model model, Authentication authentication, HttpServletResponse response) {

		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			model.addAttribute(QUESTION, question);
			model.addAttribute(USER_LOGIN, userName);
			return CREATE_QUESTION_VIEW;
		}
		UserEntity user = userManager.getUserByAuthentication(authentication);
		questionManager.createQuestion(question, user);
		return "redirect:..";
	}

	@GetMapping("/new")
	public String createQuestionView(Model model, Authentication authentication) {

		String userName = authentication.getName();
		model.addAttribute(QUESTION, new QuestionCreateUpdate());
		model.addAttribute(USER_LOGIN, userName);
		return CREATE_QUESTION_VIEW;
	}

	@PostMapping("/{questionId}/edit")
	public String editQuestion(@ModelAttribute("question") @Validated QuestionCreateUpdate question, BindingResult bindingResult,
							   Model model, @PathVariable Long questionId, Authentication authentication, HttpServletResponse response) {

		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			QuestionGet oldQuestion = questionManager.getQuestion(questionId);
			model.addAttribute(OLD_QUESTION, oldQuestion);
			model.addAttribute(QUESTION, question);
			model.addAttribute(USER_LOGIN, userName);
			return EDIT_QUESTION_VIEW;
		}
		UserEntity user = userManager.getUserByAuthentication(authentication);
		try {
			questionManager.updateQuestion(questionId, question, user);
		} catch (ChangeNotAllowedException e) {
			return getChangeNotAllowedView(model, userName, e, response);
		} catch (ResourceNotFoundException e) {
			return getResourceNotFoundView(model, userName, e, response);
		}
		return "redirect:../..";
	}

	private String getChangeNotAllowedView(Model model, String userName, ChangeNotAllowedException e,
										   HttpServletResponse response) {

		response.setStatus(e.getHttpStatus());
		model.addAttribute(USER_LOGIN, userName);
		return CHANGE_NOT_ALLOWED_VIEW;
	}

	@GetMapping("/{questionId}/edit")
	public String editQuestionView(Model model, @PathVariable Long questionId, Authentication authentication,
								   HttpServletResponse response) {

		return getEditOrRemoveQuestionView(model, questionId, authentication, true, response);
	}

	@PostMapping("/{questionId}/delete")
	public String removeQuestion(@PathVariable Long questionId, Authentication authentication, Model model,
								 HttpServletResponse response) {

		UserEntity user = userManager.getUserByAuthentication(authentication);
		String userName = authentication.getName();
		try {
			questionManager.deleteQuestion(questionId, user);
		} catch (ChangeNotAllowedException e) {
			return getChangeNotAllowedView(model, userName, e, response);
		} catch (ResourceNotFoundException e) {
			return getResourceNotFoundView(model, userName, e, response);
		}
		return "redirect:../..";
	}

	@GetMapping("/{questionId}/delete")
	public String removeQuestionView(Model model, @PathVariable Long questionId, Authentication authentication,
									 HttpServletResponse response) {

		return getEditOrRemoveQuestionView(model, questionId, authentication, false, response);
	}

	@GetMapping("/file-export")
	public ResponseEntity<Object> downloadFile(@RequestParam FileType fileType,
											   @RequestParam(required = false) String keyword,
											   @RequestParam(required = false) SortField sortField,
											   @RequestParam(required = false) Sort.Direction sortDirection) {

		sortField = Filter.checkQuestionsSortField(sortField);
		Filter filter = new Filter(keyword, sortField, sortDirection);
		return questionManager.generateQuestionsFile(fileType, filter);
	}

	private String getResourceNotFoundView(Model model, String userName, ResourceNotFoundException e,
										   HttpServletResponse response) {

		response.setStatus(e.getHttpStatus());
		model.addAttribute(USER_LOGIN, userName);
		model.addAttribute(MESSAGE, e.getMessage());
		return RESOURCE_NOT_FOUND_VIEW;
	}

	private String getEditOrRemoveQuestionView(Model model, Long questionId, Authentication authentication,
											   boolean isEdit, HttpServletResponse response) {

		String userName = authentication.getName();
		QuestionGet question;
		try {
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			return getResourceNotFoundView(model, userName, e, response);
		}
		model.addAttribute(QUESTION, question);
		model.addAttribute(USER_LOGIN, userName);
		if (isEdit) {
			model.addAttribute(OLD_QUESTION, question);
			return EDIT_QUESTION_VIEW;
		}
		return DELETE_QUESTION_VIEW;
	}
}
