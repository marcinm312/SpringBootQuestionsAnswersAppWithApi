package pl.marcinm312.springdatasecurityex.question.service;

import com.itextpdf.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.marcinm312.springdatasecurityex.config.security.utils.PermissionsUtils;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.question.model.QuestionMapper;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionCreateUpdate;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.question.repository.QuestionRepository;
import pl.marcinm312.springdatasecurityex.shared.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.shared.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.shared.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.shared.file.FileResponseGenerator;
import pl.marcinm312.springdatasecurityex.shared.file.PdfGenerator;
import pl.marcinm312.springdatasecurityex.shared.filter.Filter;
import pl.marcinm312.springdatasecurityex.shared.model.ListPage;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class QuestionManager {

	private static final String QUESTION_NOT_FOUND = "Nie znaleziono pytania o id: ";

	private final QuestionRepository questionRepository;
	private final ExcelGenerator excelGenerator;
	private final PdfGenerator pdfGenerator;

	@Autowired
	public QuestionManager(QuestionRepository questionRepository) throws DocumentException, IOException {
		this.questionRepository = questionRepository;
		this.excelGenerator = new ExcelGenerator();
		this.pdfGenerator = new PdfGenerator();
	}

	private List<QuestionGet> getQuestions(Filter filter) {
		List<QuestionEntity> questionsFromDB = questionRepository.getQuestions(Sort.by(filter.getSortDirection(),
				filter.getSortField().getField()));
		return QuestionMapper.convertQuestionEntityListToQuestionGetList(questionsFromDB);
	}

	private List<QuestionGet> searchQuestions(Filter filter) {
		if (filter.isKeywordEmpty()) {
			return getQuestions(filter);
		} else {
			List<QuestionEntity> questionsFromDB = questionRepository.searchQuestions(filter.getKeyword(),
					Sort.by(filter.getSortDirection(), filter.getSortField().getField()));
			return QuestionMapper.convertQuestionEntityListToQuestionGetList(questionsFromDB);
		}
	}

	private ListPage<QuestionGet> getPaginatedQuestions(Filter filter) {
		Page<QuestionEntity> questionEntities = questionRepository.getPaginatedQuestions(PageRequest
				.of(filter.getPageNo() - 1, filter.getPageSize(), Sort.by(filter.getSortDirection(),
						filter.getSortField().getField())));
		List<QuestionGet> questionList = QuestionMapper.convertQuestionEntityListToQuestionGetList(
				questionEntities.getContent());
		return new ListPage<>(questionList, questionEntities.getTotalPages(), questionEntities.getTotalElements());
	}

	public ListPage<QuestionGet> searchPaginatedQuestions(Filter filter) {
		if (filter.isKeywordEmpty()) {
			return getPaginatedQuestions(filter);
		} else {
			Page<QuestionEntity> questionEntities = questionRepository.searchPaginatedQuestions(filter.getKeyword(), PageRequest
					.of(filter.getPageNo() - 1, filter.getPageSize(), Sort.by(filter.getSortDirection(),
							filter.getSortField().getField())));
			List<QuestionGet> questionList = QuestionMapper.convertQuestionEntityListToQuestionGetList(
					questionEntities.getContent());
			return new ListPage<>(questionList, questionEntities.getTotalPages(), questionEntities.getTotalElements());
		}
	}

	public Optional<QuestionEntity> getQuestionEntity(Long questionId) {
		return questionRepository.findById(questionId);
	}

	public QuestionGet getQuestion(Long questionId) {
		QuestionEntity questionFromDB = questionRepository.findById(questionId)
				.orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND + questionId));
		return QuestionMapper.convertQuestionEntityToQuestionGet(questionFromDB);
	}

	public void checkIfQuestionExists(Long questionId) {
		if (!questionRepository.existsById(questionId)) {
			throw new ResourceNotFoundException(QUESTION_NOT_FOUND + questionId);
		}
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

	public ResponseEntity<Object> generateQuestionsFile(FileTypes filetype, Filter filter) throws IOException,
			DocumentException {
		List<QuestionGet> questionsList = searchQuestions(filter);
		String fileId = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
		String fileName = "Pytania_" + fileId;

		byte[] bytes;
		if (filetype == FileTypes.EXCEL) {
			fileName += ".xlsx";
			bytes = excelGenerator.generateQuestionsExcelFile(questionsList);
		} else if (filetype == FileTypes.PDF) {
			fileName += ".pdf";
			bytes = pdfGenerator.generateQuestionsPdfFile(questionsList);
		} else {
			return null;
		}
		return FileResponseGenerator.generateResponseWithFile(bytes, fileName);
	}
}
