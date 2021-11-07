package pl.marcinm312.springdatasecurityex.answer.controller;

import com.itextpdf.text.DocumentException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.shared.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.shared.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerCreateUpdate;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.answer.service.AnswerManager;
import pl.marcinm312.springdatasecurityex.question.service.QuestionManager;
import pl.marcinm312.springdatasecurityex.user.service.UserManager;

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
	private final UserManager userManager;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public AnswerWebController(QuestionManager questionManager, AnswerManager answerManager, UserManager userManager) {
		this.questionManager = questionManager;
		this.answerManager = answerManager;
		this.userManager = userManager;
	}

	@GetMapping
	public String answersGet(Model model, @PathVariable Long questionId, Authentication authentication) {
		log.info("Loading answers page for question.id = {}", questionId);
		String userName = authentication.getName();
		List<AnswerGet> answerList;
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
	public String createAnswer(@ModelAttribute("answer") @Validated AnswerCreateUpdate answer, BindingResult bindingResult,
							   Model model, @PathVariable Long questionId, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			QuestionGet question = questionManager.getQuestion(questionId);
			model.addAttribute(QUESTION, question);
			model.addAttribute(ANSWER, answer);
			model.addAttribute(USER_LOGIN, userName);
			return CREATE_ANSWER_VIEW;
		} else {
			UserEntity user = userManager.getUserByAuthentication(authentication);
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
		model.addAttribute(ANSWER, new AnswerCreateUpdate());
		model.addAttribute(USER_LOGIN, userName);
		return CREATE_ANSWER_VIEW;
	}

	@PostMapping("/{answerId}/edit")
	public String editAnswer(@ModelAttribute("answer") @Validated AnswerCreateUpdate answer, BindingResult bindingResult,
							 Model model, @PathVariable Long questionId, @PathVariable Long answerId, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			AnswerGet oldAnswer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
			QuestionGet question = questionManager.getQuestion(questionId);
			model.addAttribute(QUESTION, question);
			model.addAttribute(OLD_ANSWER, oldAnswer);
			model.addAttribute(ANSWER, answer);
			model.addAttribute(USER_LOGIN, userName);
			return EDIT_ANSWER_VIEW;
		} else {
			UserEntity user = userManager.getUserByAuthentication(authentication);
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
		return getEditOrRemoveAnswerView(model, questionId, answerId, authentication, true);
	}

	@PostMapping("/{answerId}/delete")
	public String removeAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
							   Authentication authentication, Model model) {
		UserEntity user = userManager.getUserByAuthentication(authentication);
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
		return getEditOrRemoveAnswerView(model, questionId, answerId, authentication, false);
	}

	@GetMapping("/pdf-export")
	public ResponseEntity<Object> downloadPdf(@PathVariable Long questionId) throws IOException, DocumentException {
		return answerManager.generateAnswersFile(questionId, FileTypes.PDF);
	}

	@GetMapping("/excel-export")
	public ResponseEntity<Object> downloadExcel(@PathVariable Long questionId) throws IOException, DocumentException {
		return answerManager.generateAnswersFile(questionId, FileTypes.EXCEL);
	}

	private String getResourceNotFoundView(Model model, String userName, ResourceNotFoundException e) {
		model.addAttribute(USER_LOGIN, userName);
		model.addAttribute(MESSAGE, e.getMessage());
		return RESOURCE_NOT_FOUND_VIEW;
	}

	private String getEditOrRemoveAnswerView(Model model, Long questionId, Long answerId, Authentication authentication,
											 boolean isEdit) {
		String userName = authentication.getName();
		AnswerGet answer;
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
		if (isEdit) {
			model.addAttribute(OLD_ANSWER, answer);
			return EDIT_ANSWER_VIEW;
		} else {
			return DELETE_ANSWER_VIEW;
		}
	}
}