package pl.marcinm312.springdatasecurityex.question.controller;

import com.itextpdf.text.DocumentException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.shared.filter.SortField;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.question.service.QuestionManager;
import pl.marcinm312.springdatasecurityex.shared.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.shared.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.shared.model.ListPage;
import pl.marcinm312.springdatasecurityex.shared.filter.Filter;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;

import java.io.IOException;

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

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public QuestionWebController(QuestionManager questionManager, UserManager userManager) {
		this.questionManager = questionManager;
		this.userManager = userManager;
	}

	@GetMapping
	public String questionsGet(Model model, Authentication authentication,
							   @RequestParam(required = false) String keyword,
							   @RequestParam(required = false) Integer pageNo,
							   @RequestParam(required = false) Integer pageSize,
							   @RequestParam(required = false) SortField sortField,
							   @RequestParam(required = false) Sort.Direction sortDirection) {

		log.info("Loading questions page");
		String userName = authentication.getName();
		if (sortField == null) {
			sortField = SortField.ID;
		}
		Filter filter = new Filter(keyword, pageNo, pageSize, sortField, sortDirection);
		ListPage<QuestionGet> paginatedQuestions = questionManager.searchPaginatedQuestions(filter);
		String sortDir = filter.getSortDirection().name().toUpperCase();

		model.addAttribute("questionList", paginatedQuestions.getItemsList());
		model.addAttribute("filter", filter);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("ASC") ? "DESC" : "ASC");
		model.addAttribute("totalPages", paginatedQuestions.getTotalPages());
		model.addAttribute("totalItems", paginatedQuestions.getTotalElements());
		model.addAttribute(USER_LOGIN, userName);

		return QUESTIONS_VIEW;
	}

	@PostMapping("/new")
	public String createQuestion(@ModelAttribute("question") @Validated QuestionCreateUpdate question, BindingResult bindingResult,
								 Model model, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			model.addAttribute(QUESTION, question);
			model.addAttribute(USER_LOGIN, userName);
			return CREATE_QUESTION_VIEW;
		} else {
			UserEntity user = userManager.getUserByAuthentication(authentication);
			questionManager.createQuestion(question, user);
			return "redirect:..";
		}
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
							   Model model, @PathVariable Long questionId, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			QuestionGet oldQuestion = questionManager.getQuestion(questionId);
			model.addAttribute(OLD_QUESTION, oldQuestion);
			model.addAttribute(QUESTION, question);
			model.addAttribute(USER_LOGIN, userName);
			return EDIT_QUESTION_VIEW;
		} else {
			UserEntity user = userManager.getUserByAuthentication(authentication);
			try {
				questionManager.updateQuestion(questionId, question, user);
			} catch (ChangeNotAllowedException e) {
				model.addAttribute(USER_LOGIN, userName);
				return CHANGE_NOT_ALLOWED_VIEW;
			}
			return "redirect:../..";
		}
	}

	@GetMapping("/{questionId}/edit")
	public String editQuestionView(Model model, @PathVariable Long questionId, Authentication authentication) {
		return getEditOrRemoveQuestionView(model, questionId, authentication, true);
	}

	@PostMapping("/{questionId}/delete")
	public String removeQuestion(@PathVariable Long questionId, Authentication authentication, Model model) {
		UserEntity user = userManager.getUserByAuthentication(authentication);
		try {
			questionManager.deleteQuestion(questionId, user);
		} catch (ChangeNotAllowedException e) {
			String userName = authentication.getName();
			model.addAttribute(USER_LOGIN, userName);
			return CHANGE_NOT_ALLOWED_VIEW;
		}
		return "redirect:../..";
	}

	@GetMapping("/{questionId}/delete")
	public String removeQuestionView(Model model, @PathVariable Long questionId, Authentication authentication) {
		return getEditOrRemoveQuestionView(model, questionId, authentication, false);
	}

	@GetMapping("/pdf-export")
	public ResponseEntity<Object> downloadPdf(@RequestParam(required = false) String keyword,
											  @RequestParam(required = false) SortField sortField,
											  @RequestParam(required = false) Sort.Direction sortDirection)
			throws IOException, DocumentException {

		if (sortField == null) {
			sortField = SortField.ID;
		}
		Filter filter = new Filter(keyword, sortField, sortDirection);
		return questionManager.generateQuestionsFile(FileTypes.PDF, filter);
	}

	@GetMapping("/excel-export")
	public ResponseEntity<Object> downloadExcel(@RequestParam(required = false) String keyword,
												@RequestParam(required = false) SortField sortField,
												@RequestParam(required = false) Sort.Direction sortDirection)
			throws IOException, DocumentException {

		if (sortField == null) {
			sortField = SortField.ID;
		}
		Filter filter = new Filter(keyword, sortField, sortDirection);
		return questionManager.generateQuestionsFile(FileTypes.EXCEL, filter);
	}

	private String getResourceNotFoundView(Model model, String userName, ResourceNotFoundException e) {
		model.addAttribute(USER_LOGIN, userName);
		model.addAttribute(MESSAGE, e.getMessage());
		return RESOURCE_NOT_FOUND_VIEW;
	}

	private String getEditOrRemoveQuestionView(Model model, Long questionId, Authentication authentication,
											   boolean isEdit) {
		String userName = authentication.getName();
		QuestionGet question;
		try {
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			return getResourceNotFoundView(model, userName, e);
		}
		model.addAttribute(QUESTION, question);
		model.addAttribute(USER_LOGIN, userName);
		if (isEdit) {
			model.addAttribute(OLD_QUESTION, question);
			return EDIT_QUESTION_VIEW;
		} else {
			return DELETE_QUESTION_VIEW;
		}
	}
}
