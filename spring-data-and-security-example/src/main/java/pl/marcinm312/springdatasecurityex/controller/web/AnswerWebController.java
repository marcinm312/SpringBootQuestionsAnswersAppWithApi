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
import pl.marcinm312.springdatasecurityex.service.db.AnswerManager;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;

@Controller
@RequestMapping("/app/questions/{questionId}/answers")
public class AnswerWebController {

	private QuestionManager questionManager;
	private AnswerManager answerManager;
	private PdfGenerator pdfGenerator;
	private ExcelGenerator excelGenerator;

	@Autowired
	public AnswerWebController(QuestionManager questionManager, AnswerManager answerManager, PdfGenerator pdfGenerator,
			ExcelGenerator excelGenerator) {
		this.questionManager = questionManager;
		this.answerManager = answerManager;
		this.pdfGenerator = pdfGenerator;
		this.excelGenerator = excelGenerator;
	}

	@GetMapping
	public String answersGet(Model model, @PathVariable Long questionId) {
		List<Answer> answerList = answerManager.getAnswersByQuestionId(questionId);
		Question question = questionManager.getQuestion(questionId);
		model.addAttribute("answerList", answerList);
		model.addAttribute("question", question);
		return "answers";
	}

	@PostMapping("/new")
	public String createAnswer(@ModelAttribute("answer") @Validated Answer answer, BindingResult bindingResult,
			Model model, @PathVariable Long questionId) {
		if (bindingResult.hasErrors()) {
			Question question = questionManager.getQuestion(questionId);
			model.addAttribute("question", question);
			model.addAttribute("answer", answer);
			return "createAnswer";
		} else {
			answerManager.addAnswer(questionId, answer);
			return "redirect:..";
		}
	}

	@GetMapping("/new")
	public String createAnswerView(Model model, @PathVariable Long questionId) {
		Question question = questionManager.getQuestion(questionId);
		model.addAttribute("question", question);
		model.addAttribute("answer", new Answer());
		return "createAnswer";
	}

	@PostMapping("/{answerId}/edit")
	public String editAnswer(@ModelAttribute("answer") @Validated Answer answer, BindingResult bindingResult,
			Model model, @PathVariable Long questionId, @PathVariable Long answerId) {
		if (bindingResult.hasErrors()) {
			Answer oldAnswer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
			Question question = questionManager.getQuestion(questionId);
			model.addAttribute("question", question);
			model.addAttribute("oldAnswer", oldAnswer);
			model.addAttribute("answer", answer);
			return "editAnswer";
		} else {
			answerManager.updateAnswer(questionId, answerId, answer);
			return "redirect:../..";
		}
	}

	@GetMapping("/{answerId}/edit")
	public String editAnswerView(Model model, @PathVariable Long questionId, @PathVariable Long answerId) {
		Answer answer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
		Question question = questionManager.getQuestion(questionId);
		model.addAttribute("question", question);
		model.addAttribute("oldAnswer", answer);
		model.addAttribute("answer", answer);
		return "editAnswer";
	}

	@PostMapping("/{answerId}/delete")
	public String removeAnswer(@PathVariable Long questionId, @PathVariable Long answerId) {
		answerManager.deleteAnswer(questionId, answerId);
		return "redirect:../..";
	}

	@GetMapping("/{answerId}/delete")
	public String removeAnswerView(Model model, @PathVariable Long questionId, @PathVariable Long answerId) {
		Answer answer = answerManager.getAnswerByQuestionIdAndAnswerId(questionId, answerId);
		Question question = questionManager.getQuestion(questionId);
		model.addAttribute("answer", answer);
		model.addAttribute("question", question);
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
