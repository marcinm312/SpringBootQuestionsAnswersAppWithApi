package pl.marcinm312.springdatasecurityex.controller.web;

import com.itextpdf.text.DocumentException;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.*;
import pl.marcinm312.springdatasecurityex.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.model.User;
import pl.marcinm312.springdatasecurityex.service.db.QuestionManager;
import pl.marcinm312.springdatasecurityex.service.db.UserManager;
import pl.marcinm312.springdatasecurityex.service.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.service.file.PdfGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequestMapping("/app/questions")
public class QuestionWebController {

	private final QuestionManager questionManager;
	private final PdfGenerator pdfGenerator;
	private final ExcelGenerator excelGenerator;
	private final UserManager userManager;

	protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

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
		List<Question> questionList = questionManager.getQuestions();
		log.info("questionList.size()=" + questionList.size());
		model.addAttribute("questionList", questionList);
		model.addAttribute("userLogin", userName);
		return "questions";
	}

	@PostMapping("/new")
	public String createQuestion(@ModelAttribute("question") @Validated Question question, BindingResult bindingResult,
								 Model model, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			model.addAttribute("question", question);
			model.addAttribute("userLogin", userName);
			return "createQuestion";
		} else {
			User user = userManager.getUserByAuthentication(authentication);
			questionManager.createQuestion(question, user);
			return "redirect:..";
		}
	}

	@GetMapping("/new")
	public String createQuestionView(Model model, Authentication authentication) {
		String userName = authentication.getName();
		model.addAttribute("question", new Question());
		model.addAttribute("userLogin", userName);
		return "createQuestion";
	}

	@PostMapping("/{questionId}/edit")
	public String editQuestion(@ModelAttribute("question") @Validated Question question, BindingResult bindingResult,
							   Model model, @PathVariable Long questionId, Authentication authentication) {
		String userName = authentication.getName();
		if (bindingResult.hasErrors()) {
			Question oldQuestion = questionManager.getQuestion(questionId);
			model.addAttribute("oldQuestion", oldQuestion);
			model.addAttribute("question", question);
			model.addAttribute("userLogin", userName);
			return "editQuestion";
		} else {
			User user = userManager.getUserByAuthentication(authentication);
			try {
				questionManager.updateQuestion(questionId, question, user);
			} catch (ChangeNotAllowedException e) {
				model.addAttribute("userLogin", userName);
				return "changeNotAllowed";
			}
			return "redirect:../..";
		}
	}

	@GetMapping("/{questionId}/edit")
	public String editQuestionView(Model model, @PathVariable Long questionId, Authentication authentication) {
		String userName = authentication.getName();
		Question question;
		try {
			question = questionManager.getQuestion(questionId);
		} catch (ResourceNotFoundException e) {
			model.addAttribute("userLogin", userName);
			model.addAttribute("message", e.getMessage());
			return "resourceNotFound";
		}
		model.addAttribute("oldQuestion", question);
		model.addAttribute("question", question);
		model.addAttribute("userLogin", userName);
		return "editQuestion";
	}

	@PostMapping("/{questionId}/delete")
	public String removeQuestion(@PathVariable Long questionId, Authentication authentication, Model model) {
		User user = userManager.getUserByAuthentication(authentication);
		try {
			questionManager.deleteQuestion(questionId, user);
		} catch (ChangeNotAllowedException e) {
			String userName = authentication.getName();
			model.addAttribute("userLogin", userName);
			return "changeNotAllowed";
		}
		return "redirect:../..";
	}

	@GetMapping("/{questionId}/delete")
	public String removeQuestionView(Model model, @PathVariable Long questionId, Authentication authentication) {
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
		model.addAttribute("userLogin", userName);
		return "deleteQuestion";
	}

	@GetMapping("/pdf-export")
	public ResponseEntity<?> downloadPdf() throws IOException, DocumentException {
		List<Question> questionsList = questionManager.getQuestions();
		File file = pdfGenerator.generateQuestionsPdfFile(questionsList);
		ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));
		return ResponseEntity.ok().contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
				.body(resource);
	}

	@GetMapping("/excel-export")
	public ResponseEntity<?> downloadExcel() throws IOException {
		List<Question> questionsList = questionManager.getQuestions();
		File file = excelGenerator.generateQuestionsExcelFile(questionsList);
		ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));
		return ResponseEntity.ok().contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
				.body(resource);
	}
}
