package pl.marcinm312.springquestionsanswers.answer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.marcinm312.springquestionsanswers.answer.model.AnswerEntity;
import pl.marcinm312.springquestionsanswers.answer.model.AnswerMapper;
import pl.marcinm312.springquestionsanswers.answer.model.dto.AnswerCreateUpdate;
import pl.marcinm312.springquestionsanswers.answer.model.dto.AnswerGet;
import pl.marcinm312.springquestionsanswers.answer.repository.AnswerRepository;
import pl.marcinm312.springquestionsanswers.config.security.utils.PermissionsUtils;
import pl.marcinm312.springquestionsanswers.mail.service.MailSendService;
import pl.marcinm312.springquestionsanswers.question.model.QuestionEntity;
import pl.marcinm312.springquestionsanswers.question.model.dto.QuestionGet;
import pl.marcinm312.springquestionsanswers.question.service.QuestionManager;
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

@RequiredArgsConstructor
@Slf4j
@Service
public class AnswerManager {

	private static final String QUESTION_NOT_FOUND = "Nie znaleziono pytania o id: ";
	private static final String ANSWER_NOT_FOUND = "Nie znaleziono odpowiedzi o id: %d na pytanie o id: %d";
	private static final String LOADING_ANSWER_MESSAGE = "Loading answer by: question.id={}, answer.id={}";

	private final AnswerRepository answerRepository;
	private final QuestionManager questionManager;
	private final MailSendService mailSendService;
	private final ExcelGenerator excelGenerator;
	private final PdfGenerator pdfGenerator;


	public ListPage<AnswerGet> searchPaginatedAnswers(Long questionId, Filter filter) {

		log.info("Loading answers for question.id = {}", questionId);
		questionManager.checkIfQuestionExists(questionId);
		Page<AnswerEntity> answerEntities;
		log.info(filter.toString());
		if (filter.isKeywordEmpty()) {
			answerEntities = answerRepository.getPaginatedAnswers(questionId,
					PageRequest.of(filter.getPageNo() - 1, filter.getPageSize(),
							Sort.by(filter.getSortDirection(), filter.getSortField().getField())));
		} else {
			answerEntities = answerRepository.searchPaginatedAnswers(questionId, filter.getKeyword(),
					PageRequest.of(filter.getPageNo() - 1, filter.getPageSize(),
							Sort.by(filter.getSortDirection(), filter.getSortField().getField())));
		}
		List<AnswerGet> answerList = AnswerMapper.convertAnswerEntityListToAnswerGetList(answerEntities.getContent());
		log.info("Answers list size: {}", answerList.size());
		return new ListPage<>(answerList, answerEntities.getTotalPages(), answerEntities.getTotalElements());
	}

	public AnswerGet getAnswerByQuestionIdAndAnswerId(Long questionId, Long answerId) {

		log.info(LOADING_ANSWER_MESSAGE, questionId, answerId);
		AnswerEntity answerFromDB = answerRepository.findByQuestionIdAndId(questionId, answerId)
				.orElseThrow(() -> new ResourceNotFoundException(String.format(ANSWER_NOT_FOUND, answerId, questionId)));
		return AnswerMapper.convertAnswerEntityToAnswerGet(answerFromDB, false);
	}

	@Transactional
	public AnswerGet addAnswer(Long questionId, AnswerCreateUpdate answerRequest, UserEntity user) {

		return questionManager.getQuestionEntity(questionId).map(question -> {
			AnswerEntity answer = new AnswerEntity(answerRequest.getText(), question, user);
			log.info("Adding answer = {}", answer);
			AnswerEntity savedAnswer = answerRepository.save(answer);
			sendEmail(question, "Opublikowano odpowiedź na Twoje pytanie o id: ", savedAnswer, true);
			return AnswerMapper.convertAnswerEntityToAnswerGet(savedAnswer, true);

		}).orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND + questionId));
	}

	@Transactional
	public AnswerGet updateAnswer(Long questionId, Long answerId, AnswerCreateUpdate answerRequest, UserEntity user) {

		log.info("Updating answer");
		log.info(LOADING_ANSWER_MESSAGE, questionId, answerId);
		return answerRepository.findByQuestionIdAndId(questionId, answerId).map(answer -> {

			boolean isUserPermitted = PermissionsUtils.checkIfUserIsPermitted(answer, user);
			log.info("isUserPermitted = {}", isUserPermitted);

			if (!isUserPermitted) {
				throw new ChangeNotAllowedException();
			}

			log.info("Old answer = {}", answer);
			answer.setText(answerRequest.getText());
			log.info("New answer = {}", answer);
			AnswerEntity savedAnswer = answerRepository.save(answer);
			QuestionEntity question = answer.getQuestion();
			sendEmail(question, "Zaktualizowano odpowiedź na Twoje pytanie o id: ", savedAnswer, false);
			return AnswerMapper.convertAnswerEntityToAnswerGet(savedAnswer, true);

		}).orElseThrow(() -> new ResourceNotFoundException(String.format(ANSWER_NOT_FOUND, answerId, questionId)));
	}

	private void sendEmail(QuestionEntity question, String subject, AnswerEntity savedAnswer, boolean isNewAnswer) {

		String email = question.getUser().getEmail();
		subject = subject + question.getId();
		String content = generateEmailContent(question, savedAnswer, isNewAnswer);
		mailSendService.sendMail(email, subject, content, true);
	}

	public boolean deleteAnswer(Long questionId, Long answerId, UserEntity user) {
		log.info("Deleting answer.id = {}", answerId);
		log.info(LOADING_ANSWER_MESSAGE, questionId, answerId);
		return answerRepository.findByQuestionIdAndId(questionId, answerId).map(answer -> {
			boolean isUserPermitted = PermissionsUtils.checkIfUserIsPermitted(answer, user);
			log.info("isUserPermitted = {}", isUserPermitted);

			if (!isUserPermitted) {
				throw new ChangeNotAllowedException();
			}

			answerRepository.delete(answer);
			return true;

		}).orElseThrow(() -> new ResourceNotFoundException(String.format(ANSWER_NOT_FOUND, answerId, questionId)));
	}

	public ResponseEntity<ByteArrayResource> generateAnswersFile(Long questionId, FileType filetype, Filter filter)
			throws FileException {

		QuestionGet question = questionManager.getQuestion(questionId);
		List<AnswerGet> answersList = searchPaginatedAnswers(questionId, filter).itemsList();
		String fileId = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS").format(LocalDateTime.now());
		String fileName = "Odpowiedzi_" + fileId;

		if (filetype == FileType.EXCEL) {
			fileName += ".xlsx";
			byte[] bytes = excelGenerator.generateAnswersExcelFile(answersList, question);
			return FileResponseGenerator.generateResponseWithFile(bytes, fileName,
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		} else if (filetype == FileType.PDF) {
			fileName += ".pdf";
			byte[] bytes = pdfGenerator.generateAnswersPdfFile(answersList, question);
			return FileResponseGenerator.generateResponseWithFile(bytes, fileName, "application/pdf");
		} else {
			String errorMessage = "Wspierane są tylko następujące typy plików: EXCEL, PDF";
			log.error(errorMessage);
			throw new FileException(errorMessage);
		}
	}

	private String generateEmailContent(QuestionEntity question, AnswerEntity answer, boolean isNewAnswer) {

		UserEntity questionUser = question.getUser();
		UserEntity answerUser = answer.getUser();
		String mailMessage;
		if (isNewAnswer) {
			mailMessage = "opublikował odpowiedź na Twoje pytanie:";
		} else {
			mailMessage = "zaktualizował odpowiedź na Twoje pytanie:";
		}
		String questionDescription = question.getDescription().replace("\n", "<br>");
		String answerText = answer.getText().replace("\n", "<br>");
		String mailTemplate = """
				Witaj %s,<br>
				<br>Użytkownik <b>%s</b> %s<br>
				<br><b>Tytuł:</b><br>
				%s<br>
				<br><b>Opis:</b><br>
				%s<br>
				<br>
				<br><b>Treść odpowiedzi:</b><br>
				%s""";
		return String.format(mailTemplate, questionUser.getUsername(), answerUser.getUsername(), mailMessage,
				question.getTitle(), questionDescription, answerText);
	}
}
