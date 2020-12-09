package pl.marcinm312.springdatasecurityex.controller.web;

import com.itextpdf.text.DocumentException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.Answer;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.service.db.AnswerManager;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/app/questions/{questionId}/answers")
public class AnswerWebController {

	private final QuestionManager questionManager;
	private final AnswerManager answerManager;
	private final PdfGenerator pdfGenerator;
	private final ExcelGenerator excelGenerator;
	private final UserManager userManager;

	protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

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
		log.info("Loading answers page for question.id = " + questionId);
		String userName = authentication.getName();
		List<Answer> answerList;
		Question question;
		try {
			answerList = answerManager.getAnswersByQuestionId(questionId).stream().sorted((a1, a2) -> Long.compare(a2.getId(), a1.getId()))
					.collect(Collectors.toList());
			log.info("answerList.size()=" + answerList.size());
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			model.addAttribute("userLogin", userName);
			model.addAttribute("message", e.getMessage());
			return "resourceNotFound";
		}
		model.addAttribute("answerList", answerList);
		model.addAttribute("question", question);
		model.addAttribute("userLogin", userName);
		return "answers";
	}

	@PostMapping("/new")
	public String createAnswer(@ModelAttribute("answer") @Validated Answer answer, BindingResult bindingResult,
							   Model model, @PathVariable Long questionId, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			Question question = questionManager.getQuestion(questionId);
			model.addAttribute("question", question);
			model.addAttribute("answer", answer);
			model.addAttribute("userLogin", userName);
			return "createAnswer";
		} else {
			User user = userManager.getUserByAuthentication(authentication);
			answerManager.addAnswer(questionId, answer, user);
			return "redirect:..";
		}
	}

	@GetMapping("/new")
	public String createAnswerView(Model model, @PathVariable Long questionId, Authentication authentication) {
		String userName = authentication.getName();
		Question question;
		try {
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			model.addAttribute("userLogin", userName);
			model.addAttribute("message", e.getMessage());
			return "resourceNotFound";
		}
		model.addAttribute("question", question);
		model.addAttribute("answer", new Answer());
		model.addAttribute("userLogin", userName);
		return "createAnswer";
	}

	@PostMapping("/{answerId}/edit")
	public String editAnswer(@ModelAttribute("answer") @Validated Answer answer, BindingResult bindingResult,
							 Model model, @PathVariable Long questionId, @PathVariable Long answerId, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			Answer oldAnswer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
			Question question = questionManager.getQuestion(questionId);
			model.addAttribute("question", question);
			model.addAttribute("oldAnswer", oldAnswer);
			model.addAttribute("answer", answer);
			model.addAttribute("userLogin", userName);
			return "editAnswer";
		} else {
			User user = userManager.getUserByAuthentication(authentication);
			try {
				answerManager.updateAnswer(questionId, answerId, answer, user);
			} catch (ChangeNotAllowedException e) {
				model.addAttribute("userLogin", userName);
				return "changeNotAllowed";
			}
			return "redirect:../..";
		}
	}

	@GetMapping("/{answerId}/edit")
	public String editAnswerView(Model model, @PathVariable Long questionId, @PathVariable Long answerId,
								 Authentication authentication) {
		String userName = authentication.getName();
		Answer answer;
		Question question;
		try {
			answer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			model.addAttribute("userLogin", userName);
			model.addAttribute("message", e.getMessage());
			return "resourceNotFound";
		}
		model.addAttribute("question", question);
		model.addAttribute("oldAnswer", answer);
		model.addAttribute("answer", answer);
		model.addAttribute("userLogin", userName);
		return "editAnswer";
	}

	@PostMapping("/{answerId}/delete")
	public String removeAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
							   Authentication authentication, Model model) {
		User user = userManager.getUserByAuthentication(authentication);
		try {
			answerManager.deleteAnswer(questionId, answerId, user);
		} catch (ChangeNotAllowedException e) {
			String userName = authentication.getName();
			model.addAttribute("userLogin", userName);
			return "changeNotAllowed";
		}
		return "redirect:../..";
	}

	@GetMapping("/{answerId}/delete")
	public String removeAnswerView(Model model, @PathVariable Long questionId, @PathVariable Long answerId,
								   Authentication authentication) {
		String userName = authentication.getName();
		Answer answer;
		Question question;
		try {
			answer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			model.addAttribute("userLogin", userName);
			model.addAttribute("message", e.getMessage());
			return "resourceNotFound";
		}
		model.addAttribute("answer", answer);
		model.addAttribute("question", question);
		model.addAttribute("userLogin", userName);
		return "deleteAnswer";
	}

	@GetMapping("/pdf-export")
	public ResponseEntity<?> downloadPdf(@PathVariable Long questionId) throws IOException, DocumentException {
		Question question;
		List<Answer> answersList;
		try {
			question = questionManager.getQuestion(questionId);
			answersList = answerManager.getAnswersByQuestionId(questionId);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
		File file = pdfGenerator.generateAnswersPdfFile(answersList, question);
		ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));
		return ResponseEntity.ok().contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
				.body(resource);
	}

	@GetMapping("/excel-export")
	public ResponseEntity<?> downloadExcel(@PathVariable Long questionId) throws IOException {
		Question question;
		List<Answer> answersList;
		try {
			question = questionManager.getQuestion(questionId);
			answersList = answerManager.getAnswersByQuestionId(questionId);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
		File file = excelGenerator.generateAnswersExcelFile(answersList, question);
		ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));
		return ResponseEntity.ok().contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
				.body(resource);
	}
}
