package pl.marcinm312.springdatasecurityex.controller.web;

import com.itextpdf.text.DocumentException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.FileResponseGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/app/questions")
public class QuestionWebController {

	private static final String QUESTION_LIST = "questionList";
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
	private final PdfGenerator pdfGenerator;
	private final ExcelGenerator excelGenerator;
	private final UserManager userManager;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public QuestionWebController(QuestionManager questionManager, PdfGenerator pdfGenerator,
								 ExcelGenerator excelGenerator, UserManager userManager) {
		this.questionManager = questionManager;
		this.pdfGenerator = pdfGenerator;
		this.excelGenerator = excelGenerator;
		this.userManager = userManager;
	}

	@GetMapping
	public String questionsGet(Model model, Authentication authentication) {
		log.info("Loading questions page");
		String userName = authentication.getName();
		List<QuestionGet> questionList = questionManager.getQuestions();
		log.info("questionList.size()={}", questionList.size());
		model.addAttribute(QUESTION_LIST, questionList);
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
			User user = userManager.getUserByAuthentication(authentication);
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
			User user = userManager.getUserByAuthentication(authentication);
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
		String userName = authentication.getName();
		QuestionGet question;
		try {
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			model.addAttribute(USER_LOGIN, userName);
			model.addAttribute(MESSAGE, e.getMessage());
			return RESOURCE_NOT_FOUND_VIEW;
		}
		model.addAttribute(OLD_QUESTION, question);
		model.addAttribute(QUESTION, question);
		model.addAttribute(USER_LOGIN, userName);
		return EDIT_QUESTION_VIEW;
	}

	@PostMapping("/{questionId}/delete")
	public String removeQuestion(@PathVariable Long questionId, Authentication authentication, Model model) {
		User user = userManager.getUserByAuthentication(authentication);
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
		String userName = authentication.getName();
		QuestionGet question;
		try {
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			model.addAttribute(USER_LOGIN, userName);
			model.addAttribute(MESSAGE, e.getMessage());
			return RESOURCE_NOT_FOUND_VIEW;
		}
		model.addAttribute(QUESTION, question);
		model.addAttribute(USER_LOGIN, userName);
		return DELETE_QUESTION_VIEW;
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
