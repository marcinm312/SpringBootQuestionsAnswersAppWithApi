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
import pl.marcinm312.springquestionsanswers.shared.file.FileType;
import pl.marcinm312.springquestionsanswers.shared.exception.ChangeNotAllowedException;
import pl.marcinm312.springquestionsanswers.shared.exception.FileException;
import pl.marcinm312.springquestionsanswers.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springquestionsanswers.shared.file.ExcelGenerator;
import pl.marcinm312.springquestionsanswers.shared.file.FileResponseGenerator;
import pl.marcinm312.springquestionsanswers.shared.file.PdfGenerator;
import pl.marcinm312.springquestionsanswers.shared.filter.Filter;
import pl.marcinm312.springquestionsanswers.shared.model.ListPage;
import pl.marcinm312.springquestionsanswers.user.model.UserEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class QuestionManager {

	private static final String QUESTION_NOT_FOUND = "Nie znaleziono pytania o id: ";

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

		log.info("Loading question entity with id: {}", questionId);
		return questionRepository.findById(questionId);
	}

	public QuestionGet getQuestion(Long questionId) {

		log.info("Loading question with id: {}", questionId);
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

		log.info("Updating question with id: {}", questionId);

		return questionRepository.findById(questionId).map(question -> {

			if (!PermissionsUtils.checkIfUserIsPermitted(question, user)) {
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

		log.info("Deleting question with id: {}", questionId);

		return questionRepository.findById(questionId).map(question -> {

			if (!PermissionsUtils.checkIfUserIsPermitted(question, user)) {
				throw new ChangeNotAllowedException();
			}
			questionRepository.delete(question);
			return true;

		}).orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND + questionId));
	}

	public ResponseEntity<ByteArrayResource> generateQuestionsFile(FileType fileType, Filter filter) throws FileException {

		List<QuestionGet> questionsList = searchPaginatedQuestions(filter).itemsList();
		String fileId = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS").format(LocalDateTime.now());
		String fileName = "Pytania_" + fileId;

		switch (fileType) {

			case EXCEL:
				fileName += ".xlsx";
				return FileResponseGenerator.generateResponseWithFile(
						excelGenerator.generateQuestionsExcelFile(questionsList), fileName,
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

			case PDF:
				fileName += ".pdf";
				return FileResponseGenerator.generateResponseWithFile(
						pdfGenerator.generateQuestionsPdfFile(questionsList), fileName,
						"application/pdf");

			default:
				String errorMessage = "Wspierane są tylko następujące typy plików: EXCEL, PDF";
				log.error(errorMessage);
				throw new FileException(errorMessage);
		}
	}
}
