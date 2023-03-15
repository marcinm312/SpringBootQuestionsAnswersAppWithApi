package pl.marcinm312.springquestionsanswers.question.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.marcinm312.springquestionsanswers.config.security.utils.PermissionsUtils;
import pl.marcinm312.springquestionsanswers.question.model.QuestionEntity;
import pl.marcinm312.springquestionsanswers.question.model.QuestionMapper;
import pl.marcinm312.springquestionsanswers.question.model.dto.QuestionCreateUpdate;
import pl.marcinm312.springquestionsanswers.question.model.dto.QuestionGet;
import pl.marcinm312.springquestionsanswers.question.repository.QuestionRepository;
import pl.marcinm312.springquestionsanswers.shared.enums.FileType;
import pl.marcinm312.springquestionsanswers.shared.exception.ChangeNotAllowedException;
import pl.marcinm312.springquestionsanswers.shared.exception.FileException;
import pl.marcinm312.springquestionsanswers.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springquestionsanswers.shared.file.ExcelGenerator;
import pl.marcinm312.springquestionsanswers.shared.file.FileResponseGenerator;
import pl.marcinm312.springquestionsanswers.shared.file.PdfGenerator;
import pl.marcinm312.springquestionsanswers.shared.filter.Filter;
import pl.marcinm312.springquestionsanswers.shared.model.ListPage;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class QuestionManager {

	private static final String QUESTION_NOT_FOUND = "Nie znaleziono pytania o id: ";
	private static final String LOADING_QUESTION_MESSAGE = "Loading question with id: {}";

	private final QuestionRepository questionRepository;
	private final ExcelGenerator excelGenerator;
	private final PdfGenerator pdfGenerator;


	public ListPage<QuestionGet> searchPaginatedQuestions(Filter filter) {

		log.info("Loading questions");
		Page<QuestionEntity> questionEntities;
		log.info(filter.toString());
		if (filter.isKeywordEmpty()) {
			questionEntities = questionRepository.getPaginatedQuestions(PageRequest
					.of(filter.getPageNo() - 1, filter.getPageSize(), Sort.by(filter.getSortDirection(),
							filter.getSortField().getField())));
		} else {
			questionEntities = questionRepository.searchPaginatedQuestions(filter.getKeyword(), PageRequest
					.of(filter.getPageNo() - 1, filter.getPageSize(), Sort.by(filter.getSortDirection(),
							filter.getSortField().getField())));
		}
		List<QuestionGet> questionList = QuestionMapper.convertQuestionEntityListToQuestionGetList(
				questionEntities.getContent());
		log.info("Questions list size: {}", questionList.size());
		return new ListPage<>(questionList, questionEntities.getTotalPages(), questionEntities.getTotalElements());
	}

	public Optional<QuestionEntity> getQuestionEntity(Long questionId) {
		log.info(LOADING_QUESTION_MESSAGE, questionId);
		return questionRepository.findById(questionId);
	}

	public QuestionGet getQuestion(Long questionId) {

		log.info(LOADING_QUESTION_MESSAGE, questionId);
		QuestionEntity questionFromDB = questionRepository.findById(questionId)
				.orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND + questionId));
		return QuestionMapper.convertQuestionEntityToQuestionGet(questionFromDB, false);
	}

	public void checkIfQuestionExists(Long questionId) {
		if (!questionRepository.existsById(questionId)) {
			throw new ResourceNotFoundException(QUESTION_NOT_FOUND + questionId);
		}
	}

	public QuestionGet createQuestion(QuestionCreateUpdate questionRequest, UserEntity user) {

		QuestionEntity question = new QuestionEntity(questionRequest.getTitle(), questionRequest.getDescription(), user);
		log.info("Creating question = {}", question);
		return QuestionMapper.convertQuestionEntityToQuestionGet(questionRepository.save(question), true);
	}

	@Transactional
	public QuestionGet updateQuestion(Long questionId, QuestionCreateUpdate questionRequest, UserEntity user) {

		log.info("Updating question");
		log.info(LOADING_QUESTION_MESSAGE, questionId);
		return questionRepository.findById(questionId).map(question -> {
			boolean isUserPermitted = PermissionsUtils.checkIfUserIsPermitted(question, user);
			log.info("isUserPermitted = {}", isUserPermitted);

			if (!isUserPermitted) {
				throw new ChangeNotAllowedException();
			}

			log.info("Old question = {}", question);
			question.setTitle(questionRequest.getTitle());
			question.setDescription(questionRequest.getDescription());
			log.info("New question = {}", question);
			return QuestionMapper.convertQuestionEntityToQuestionGet(questionRepository.save(question), true);

		}).orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND + questionId));
	}

	public boolean deleteQuestion(Long questionId, UserEntity user) {

		log.info("Deleting question.id = {}", questionId);
		return questionRepository.findById(questionId).map(question -> {
			boolean isUserPermitted = PermissionsUtils.checkIfUserIsPermitted(question, user);
			log.info("isUserPermitted = {}", isUserPermitted);

			if (!isUserPermitted) {
				throw new ChangeNotAllowedException();
			}

			questionRepository.delete(question);
			return true;

		}).orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND + questionId));
	}

	public ResponseEntity<ByteArrayResource> generateQuestionsFile(FileType filetype, Filter filter) throws FileException {

		List<QuestionGet> questionsList = searchPaginatedQuestions(filter).itemsList();
		String fileId = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
		String fileName = "Pytania_" + fileId;

		byte[] bytes = null;
		if (filetype == FileType.EXCEL) {
			fileName += ".xlsx";
			bytes = excelGenerator.generateQuestionsExcelFile(questionsList);
		} else if (filetype == FileType.PDF) {
			fileName += ".pdf";
			bytes = pdfGenerator.generateQuestionsPdfFile(questionsList);
		}

		if (bytes == null) {
			String errorMessage = "Wspierane są tylko następujące typy plików: EXCEL, PDF";
			log.error(errorMessage);
			throw new FileException(errorMessage);
		}
		return FileResponseGenerator.generateResponseWithFile(bytes, fileName);
	}
}
