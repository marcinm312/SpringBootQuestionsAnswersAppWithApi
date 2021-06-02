package pl.marcinm312.springdatasecurityex.controller.web;

import com.itextpdf.text.DocumentException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.answer.Answer;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.service.db.AnswerManager;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.FileResponseGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/app/questions/{questionId}/answers")
public class AnswerWebController {

	private static final String USER_LOGIN = "userLogin";
	private static final String MESSAGE = "message";
	private static final String RESOURCE_NOT_FOUND_VIEW = "resourceNotFound";
	private static final String ANSWER_LIST = "answerList";
	private static final String QUESTION = "question";
	private static final String ANSWER = "answer";
	private static final String ANSWERS_VIEW = "answers";
	private static final String CREATE_ANSWER_VIEW = "createAnswer";
	private static final String OLD_ANSWER = "oldAnswer";
	private static final String EDIT_ANSWER_VIEW = "editAnswer";
	private static final String CHANGE_NOT_ALLOWED_VIEW = "changeNotAllowed";
	private static final String DELETE_ANSWER_VIEW = "deleteAnswer";

	private final QuestionManager questionManager;
	private final AnswerManager answerManager;
	private final PdfGenerator pdfGenerator;
	private final ExcelGenerator excelGenerator;
	private final UserManager userManager;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public AnswerWebController(QuestionManager questionManager, AnswerManager answerManager, PdfGenerator pdfGenerator,
							   ExcelGenerator excelGenerator, UserManager userManager) {
		this.questionManager = questionManager;
		this.answerManager = answerManager;
		this.pdfGenerator = pdfGenerator;
		this.excelGenerator = excelGenerator;
		this.userManager = userManager;
	}

	@GetMapping
	public String answersGet(Model model, @PathVariable Long questionId, Authentication authentication) {
		log.info("Loading answers page for question.id = {}", questionId);
		String userName = authentication.getName();
		List<Answer> answerList;
		QuestionGet question;
		try {
			answerList = answerManager.getAnswersByQuestionId(questionId);
			log.info("answerList.size()={}", answerList.size());
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			return getResourceNotFoundView(model, userName, e);
		}
		model.addAttribute(ANSWER_LIST, answerList);
		model.addAttribute(QUESTION, question);
		model.addAttribute(USER_LOGIN, userName);
		return ANSWERS_VIEW;
	}

	@PostMapping("/new")
	public String createAnswer(@ModelAttribute("answer") @Validated Answer answer, BindingResult bindingResult,
							   Model model, @PathVariable Long questionId, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			QuestionGet question = questionManager.getQuestion(questionId);
			model.addAttribute(QUESTION, question);
			model.addAttribute(ANSWER, answer);
			model.addAttribute(USER_LOGIN, userName);
			return CREATE_ANSWER_VIEW;
		} else {
			User user = userManager.getUserByAuthentication(authentication);
			answerManager.addAnswer(questionId, answer, user);
			return "redirect:..";
		}
	}

	@GetMapping("/new")
	public String createAnswerView(Model model, @PathVariable Long questionId, Authentication authentication) {
		String userName = authentication.getName();
		QuestionGet question;
		try {
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			return getResourceNotFoundView(model, userName, e);
		}
		model.addAttribute(QUESTION, question);
		model.addAttribute(ANSWER, new Answer());
		model.addAttribute(USER_LOGIN, userName);
		return CREATE_ANSWER_VIEW;
	}

	@PostMapping("/{answerId}/edit")
	public String editAnswer(@ModelAttribute("answer") @Validated Answer answer, BindingResult bindingResult,
							 Model model, @PathVariable Long questionId, @PathVariable Long answerId, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			Answer oldAnswer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
			QuestionGet question = questionManager.getQuestion(questionId);
			model.addAttribute(QUESTION, question);
			model.addAttribute(OLD_ANSWER, oldAnswer);
			model.addAttribute(ANSWER, answer);
			model.addAttribute(USER_LOGIN, userName);
			return EDIT_ANSWER_VIEW;
		} else {
			User user = userManager.getUserByAuthentication(authentication);
			try {
				answerManager.updateAnswer(questionId, answerId, answer, user);
			} catch (ChangeNotAllowedException e) {
				model.addAttribute(USER_LOGIN, userName);
				return CHANGE_NOT_ALLOWED_VIEW;
			}
			return "redirect:../..";
		}
	}

	@GetMapping("/{answerId}/edit")
	public String editAnswerView(Model model, @PathVariable Long questionId, @PathVariable Long answerId,
								 Authentication authentication) {
		String userName = authentication.getName();
		Answer answer;
		QuestionGet question;
		try {
			answer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			return getResourceNotFoundView(model, userName, e);
		}
		model.addAttribute(QUESTION, question);
		model.addAttribute(OLD_ANSWER, answer);
		model.addAttribute(ANSWER, answer);
		model.addAttribute(USER_LOGIN, userName);
		return EDIT_ANSWER_VIEW;
	}

	@PostMapping("/{answerId}/delete")
	public String removeAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
							   Authentication authentication, Model model) {
		User user = userManager.getUserByAuthentication(authentication);
		try {
			answerManager.deleteAnswer(questionId, answerId, user);
		} catch (ChangeNotAllowedException e) {
			String userName = authentication.getName();
			model.addAttribute(USER_LOGIN, userName);
			return CHANGE_NOT_ALLOWED_VIEW;
		}
		return "redirect:../..";
	}

	@GetMapping("/{answerId}/delete")
	public String removeAnswerView(Model model, @PathVariable Long questionId, @PathVariable Long answerId,
								   Authentication authentication) {
		String userName = authentication.getName();
		Answer answer;
		QuestionGet question;
		try {
			answer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			return getResourceNotFoundView(model, userName, e);
		}
		model.addAttribute(ANSWER, answer);
		model.addAttribute(QUESTION, question);
		model.addAttribute(USER_LOGIN, userName);
		return DELETE_ANSWER_VIEW;
	}

	@GetMapping("/pdf-export")
	public ResponseEntity<?> downloadPdf(@PathVariable Long questionId) throws IOException, DocumentException {
		QuestionGet question;
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
		QuestionGet question;
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

	private String getResourceNotFoundView(Model model, String userName, ResourceNotFoundException e) {
		model.addAttribute(USER_LOGIN, userName);
		model.addAttribute(MESSAGE, e.getMessage());
		return RESOURCE_NOT_FOUND_VIEW;
	}
}
