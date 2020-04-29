package pl.marcinm312.springdatasecurityex.controller.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lowagie.text.DocumentException;

import pl.marcinm312.springdatasecurityex.model.Answer;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.service.db.AnswerManager;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;

@Controller
@RequestMapping("/app/questions/{questionId}/answers")
public class AnswerWebController {

	private QuestionManager questionManager;
	private AnswerManager answerManager;
	private PdfGenerator pdfGenerator;
	private ExcelGenerator excelGenerator;
	private UserManager userManager;

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
		String userName = authentication.getName();
		List<Answer> answerList = answerManager.getAnswersByQuestionId(questionId);
		Question question = questionManager.getQuestion(questionId);
		model.addAttribute("answerList", answerList);
		model.addAttribute("question", question);
		model.addAttribute("userlogin", userName);
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
			model.addAttribute("userlogin", userName);
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
		Question question = questionManager.getQuestion(questionId);
		model.addAttribute("question", question);
		model.addAttribute("answer", new Answer());
		model.addAttribute("userlogin", userName);
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
			model.addAttribute("userlogin", userName);
			return "editAnswer";
		} else {
			User user = userManager.getUserByAuthentication(authentication);
			answerManager.updateAnswer(questionId, answerId, answer, user);
			return "redirect:../..";
		}
	}

	@GetMapping("/{answerId}/edit")
	public String editAnswerView(Model model, @PathVariable Long questionId, @PathVariable Long answerId,
			Authentication authentication) {
		String userName = authentication.getName();
		Answer answer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
		Question question = questionManager.getQuestion(questionId);
		model.addAttribute("question", question);
		model.addAttribute("oldAnswer", answer);
		model.addAttribute("answer", answer);
		model.addAttribute("userlogin", userName);
		return "editAnswer";
	}

	@PostMapping("/{answerId}/delete")
	public String removeAnswer(@PathVariable Long questionId, @PathVariable Long answerId,
			Authentication authentication) {
		User user = userManager.getUserByAuthentication(authentication);
		answerManager.deleteAnswer(questionId, answerId, user);
		return "redirect:../..";
	}

	@GetMapping("/{answerId}/delete")
	public String removeAnswerView(Model model, @PathVariable Long questionId, @PathVariable Long answerId,
			Authentication authentication) {
		String userName = authentication.getName();
		Answer answer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
		Question question = questionManager.getQuestion(questionId);
		model.addAttribute("answer", answer);
		model.addAttribute("question", question);
		model.addAttribute("userlogin", userName);
		return "deleteAnswer";
	}

	@GetMapping("/pdf-export")
	public ResponseEntity<?> downloadPdf(@PathVariable Long questionId) throws IOException, DocumentException {
		Question question = questionManager.getQuestion(questionId);
		List<Answer> answersList = answerManager.getAnswersByQuestionId(questionId);
		File file = pdfGenerator.generateAnswersPdfFile(answersList, question);
		Path path = file.toPath();
		ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
		return ResponseEntity.ok().contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
				.body(resource);
	}

	@GetMapping("/excel-export")
	public ResponseEntity<?> downloadExcel(@PathVariable Long questionId) throws IOException {
		Question question = questionManager.getQuestion(questionId);
		List<Answer> answersList = answerManager.getAnswersByQuestionId(questionId);
		File file = excelGenerator.generateAnswersExcelFile(answersList, question);
		Path path = file.toPath();
		ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
		return ResponseEntity.ok().contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
				.body(resource);
	}
}
