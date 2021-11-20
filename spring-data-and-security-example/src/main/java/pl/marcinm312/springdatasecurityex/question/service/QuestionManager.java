package pl.marcinm312.springdatasecurityex.question.service;

import com.itextpdf.text.DocumentException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.shared.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.shared.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.question.model.QuestionMapper;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;
import pl.marcinm312.springdatasecurityex.question.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.shared.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.shared.file.FileResponseGenerator;
import pl.marcinm312.springdatasecurityex.shared.file.PdfGenerator;
import pl.marcinm312.springdatasecurityex.config.security.utils.PermissionsUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionManager {

	private static final String QUESTION_NOT_FOUND = "Nie znaleziono pytania o id: ";

	private final QuestionRepository questionRepository;
	private final ExcelGenerator excelGenerator;
	private final PdfGenerator pdfGenerator;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public QuestionManager(QuestionRepository questionRepository, ExcelGenerator excelGenerator,
						   PdfGenerator pdfGenerator) {
		this.questionRepository = questionRepository;
		this.excelGenerator = excelGenerator;
		this.pdfGenerator = pdfGenerator;
	}

	public List<QuestionGet> getQuestions() {
		List<QuestionEntity> questionsFromDB = questionRepository.findAllByOrderByIdDesc();
		return QuestionMapper.convertQuestionEntityListToQuestionGetList(questionsFromDB);
	}

	public Optional<QuestionEntity> getQuestionEntity(Long questionId) {
		return questionRepository.findById(questionId);
	}

	public QuestionGet getQuestion(Long questionId) {
		QuestionEntity questionFromDB = questionRepository.findById(questionId)
				.orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND + questionId));
		return QuestionMapper.convertQuestionEntityToQuestionGet(questionFromDB);
	}

	public boolean checkIfQuestionExists(Long questionId) {
		return questionRepository.existsById(questionId);
	}

	public QuestionGet createQuestion(QuestionCreateUpdate questionRequest, UserEntity user) {
		QuestionEntity question = new QuestionEntity(questionRequest.getTitle(), questionRequest.getDescription());
		question.setUser(user);
		log.info("Creating question = {}", question);
		return QuestionMapper.convertQuestionEntityToQuestionGet(questionRepository.save(question));
	}

	public QuestionGet updateQuestion(Long questionId, QuestionCreateUpdate questionRequest, UserEntity user) {
		log.info("Updating question");
		return questionRepository.findById(questionId).map(question -> {
			boolean isUserPermitted = PermissionsUtils.checkIfUserIsPermitted(question, user);
			log.info("isUserPermitted = {}", isUserPermitted);
			if (isUserPermitted) {
				log.info("Old question = {}", question);
				question.setTitle(questionRequest.getTitle());
				question.setDescription(questionRequest.getDescription());
				log.info("New question = {}", question);
				return QuestionMapper.convertQuestionEntityToQuestionGet(questionRepository.save(question));
			} else {
				throw new ChangeNotAllowedException();
			}
		}).orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND + questionId));
	}

	public boolean deleteQuestion(Long questionId, UserEntity user) {
		log.info("Deleting question.id = {}", questionId);
		return questionRepository.findById(questionId).map(question -> {
			boolean isUserPermitted = PermissionsUtils.checkIfUserIsPermitted(question, user);
			log.info("isUserPermitted = {}", isUserPermitted);
			if (isUserPermitted) {
				questionRepository.delete(question);
				return true;
			} else {
				throw new ChangeNotAllowedException();
			}
		}).orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND + questionId));
	}

	public ResponseEntity<Object> generateQuestionsFile(FileTypes filetype) throws IOException, DocumentException {
		List<QuestionGet> questionsList = getQuestions();
		File file;
		if (filetype.equals(FileTypes.EXCEL)) {
			file = excelGenerator.generateQuestionsExcelFile(questionsList);
		} else {
			file = pdfGenerator.generateQuestionsPdfFile(questionsList);
		}
		return FileResponseGenerator.generateResponseWithFile(file);
	}
}
