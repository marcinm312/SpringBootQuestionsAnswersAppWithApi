package pl.marcinm312.springdatasecurityex.service.db;

import com.itextpdf.text.DocumentException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.marcinm312.springdatasecurityex.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.question.Question;
import pl.marcinm312.springdatasecurityex.model.question.QuestionMapper;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.utils.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.utils.file.FileResponseGenerator;
import pl.marcinm312.springdatasecurityex.utils.file.PdfGenerator;
import pl.marcinm312.springdatasecurityex.utils.PermissionsUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionManager {

	private static final String QUESTION_NOT_FOUND_WITH_ID = "Question not found with id: ";

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
		List<Question> questionsFromDB = questionRepository.findAllByOrderByIdDesc();
		return QuestionMapper.convertQuestionListToQuestionGetList(questionsFromDB);
	}

	public Optional<Question> getQuestionEntity(Long questionId) {
		return questionRepository.findById(questionId);
	}

	public QuestionGet getQuestion(Long questionId) {
		Question questionFromDB = questionRepository.findById(questionId)
				.orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND_WITH_ID + questionId));
		return QuestionMapper.convertQuestionToQuestionGet(questionFromDB);
	}

	public QuestionGet createQuestion(QuestionCreateUpdate questionRequest, User user) {
		Question question = new Question(questionRequest.getTitle(), questionRequest.getDescription());
		question.setUser(user);
		log.info("Creating question = {}", question);
		return QuestionMapper.convertQuestionToQuestionGet(questionRepository.save(question));
	}

	public QuestionGet updateQuestion(Long questionId, QuestionCreateUpdate questionRequest, User user) {
		log.info("Updating question");
		return questionRepository.findById(questionId).map(question -> {
			boolean isUserPermitted = PermissionsUtils.checkIfUserIsPermitted(question, user);
			log.info("isUserPermitted = {}", isUserPermitted);
			if (isUserPermitted) {
				log.info("Old question = {}", question);
				question.setTitle(questionRequest.getTitle());
				question.setDescription(questionRequest.getDescription());
				log.info("New question = {}", question);
				return QuestionMapper.convertQuestionToQuestionGet(questionRepository.save(question));
			} else {
				throw new ChangeNotAllowedException();
			}
		}).orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND_WITH_ID + questionId));
	}

	public boolean deleteQuestion(Long questionId, User user) {
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
		}).orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND_WITH_ID + questionId));
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
