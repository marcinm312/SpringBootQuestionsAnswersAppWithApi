package pl.marcinm312.springdatasecurityex.answer.service;

import com.itextpdf.text.DocumentException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.marcinm312.springdatasecurityex.answer.model.AnswerEntity;
import pl.marcinm312.springdatasecurityex.answer.model.AnswerMapper;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerCreateUpdate;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.answer.repository.AnswerRepository;
import pl.marcinm312.springdatasecurityex.config.security.utils.PermissionsUtils;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.question.service.QuestionManager;
import pl.marcinm312.springdatasecurityex.shared.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.shared.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.shared.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.shared.file.FileResponseGenerator;
import pl.marcinm312.springdatasecurityex.shared.file.PdfGenerator;
import pl.marcinm312.springdatasecurityex.shared.filter.Filter;
import pl.marcinm312.springdatasecurityex.shared.mail.MailService;
import pl.marcinm312.springdatasecurityex.shared.model.ListPage;
import pl.marcinm312.springdatasecurityex.user.model.UserEntity;

import javax.mail.MessagingException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class AnswerManager {

	private static final String QUESTION_NOT_FOUND = "Nie znaleziono pytania o id: ";
	private static final String ANSWER_NOT_FOUND = "Nie znaleziono odpowiedzi o id: %d na pytanie o id: %d";

	private final AnswerRepository answerRepository;
	private final QuestionManager questionManager;
	private final MailService mailService;
	private final ExcelGenerator excelGenerator;
	private final PdfGenerator pdfGenerator;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public AnswerManager(AnswerRepository answerRepository, QuestionManager questionManager,
						 MailService mailService) throws DocumentException, IOException {
		this.answerRepository = answerRepository;
		this.questionManager = questionManager;
		this.mailService = mailService;
		this.excelGenerator = new ExcelGenerator();
		this.pdfGenerator = new PdfGenerator();
	}

	private List<AnswerGet> getAnswers(Long questionId, Filter filter) {
		List<AnswerEntity> answersFromDB = answerRepository.getAnswers(questionId,
				Sort.by(filter.getSortDirection(), filter.getSortField().getField()));
		return AnswerMapper.convertAnswerEntityListToAnswerGetList(answersFromDB);
	}

	private List<AnswerGet> searchAnswers(Long questionId, Filter filter) {
		questionManager.checkIfQuestionExists(questionId);
		if (filter.isKeywordEmpty()) {
			return getAnswers(questionId, filter);
		} else {
			List<AnswerEntity> answersFromDB = answerRepository.searchAnswers(questionId, filter.getKeyword(),
					Sort.by(filter.getSortDirection(), filter.getSortField().getField()));
			return AnswerMapper.convertAnswerEntityListToAnswerGetList(answersFromDB);
		}
	}

	private ListPage<AnswerGet> getPaginatedAnswers(Long questionId, Filter filter) {
		Page<AnswerEntity> answerEntities = answerRepository.getPaginatedAnswers(questionId,
				PageRequest.of(filter.getPageNo() - 1, filter.getPageSize(),
						Sort.by(filter.getSortDirection(), filter.getSortField().getField())));
		List<AnswerGet> answerList = AnswerMapper.convertAnswerEntityListToAnswerGetList(answerEntities.getContent());
		return new ListPage<>(answerList, answerEntities.getTotalPages(), answerEntities.getTotalElements());
	}

	public ListPage<AnswerGet> searchPaginatedAnswers(Long questionId, Filter filter) {
		questionManager.checkIfQuestionExists(questionId);
		if (filter.isKeywordEmpty()) {
			return getPaginatedAnswers(questionId, filter);
		} else {
			Page<AnswerEntity> answerEntities = answerRepository.searchPaginatedAnswers(questionId, filter.getKeyword(),
					PageRequest.of(filter.getPageNo() - 1, filter.getPageSize(),
							Sort.by(filter.getSortDirection(), filter.getSortField().getField())));
			List<AnswerGet> answerList = AnswerMapper.convertAnswerEntityListToAnswerGetList(answerEntities.getContent());
			return new ListPage<>(answerList, answerEntities.getTotalPages(), answerEntities.getTotalElements());
		}
	}

	public AnswerGet getAnswerByQuestionIdAndAnswerId(Long questionId, Long answerId) {
		AnswerEntity answerFromDB = answerRepository.findByQuestionIdAndId(questionId, answerId)
				.orElseThrow(() -> new ResourceNotFoundException(String.format(ANSWER_NOT_FOUND, answerId, questionId)));
		return AnswerMapper.convertAnswerEntityToAnswerGet(answerFromDB);
	}

	@Transactional
	public AnswerGet addAnswer(Long questionId, AnswerCreateUpdate answerRequest, UserEntity user) {
		return questionManager.getQuestionEntity(questionId).map(question -> {
			AnswerEntity answer = new AnswerEntity(answerRequest.getText());
			answer.setQuestion(question);
			answer.setUser(user);
			log.info("Adding answer = {}", answer);
			AnswerEntity savedAnswer = answerRepository.save(answer);
			try {
				String email = question.getUser().getEmail();
				String subject = "Opublikowano odpowiedź na Twoje pytanie o id: " + question.getId();
				String content = generateEmailContent(question, savedAnswer, true);
				mailService.sendMail(email, subject, content, true);
			} catch (MessagingException e) {
				log.error("An error occurred while sending the email. [MESSAGE]: {}", e.getMessage());
			}
			return AnswerMapper.convertAnswerEntityToAnswerGet(savedAnswer);

		}).orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND + questionId));
	}

	@Transactional
	public AnswerGet updateAnswer(Long questionId, Long answerId, AnswerCreateUpdate answerRequest, UserEntity user) {
		log.info("Updating answer");
		return answerRepository.findByQuestionIdAndId(questionId, answerId).map(answer -> {
			boolean isUserPermitted = PermissionsUtils.checkIfUserIsPermitted(answer, user);
			log.info("isUserPermitted = {}", isUserPermitted);
			if (isUserPermitted) {
				log.info("Old answer = {}", answer);
				answer.setText(answerRequest.getText());
				log.info("New answer = {}", answer);
				AnswerEntity savedAnswer = answerRepository.save(answer);
				try {
					QuestionEntity question = answer.getQuestion();
					String email = question.getUser().getEmail();
					String subject = "Zaktualizowano odpowiedź na Twoje pytanie o id: " + question.getId();
					String content = generateEmailContent(question, savedAnswer, false);
					mailService.sendMail(email, subject, content, true);
				} catch (MessagingException e) {
					log.error("An error occurred while sending the email. [MESSAGE]: {}", e.getMessage());
				}
				return AnswerMapper.convertAnswerEntityToAnswerGet(savedAnswer);
			} else {
				throw new ChangeNotAllowedException();
			}
		}).orElseThrow(() -> new ResourceNotFoundException(String.format(ANSWER_NOT_FOUND, answerId, questionId)));
	}

	public boolean deleteAnswer(Long questionId, Long answerId, UserEntity user) {
		log.info("Deleting answer.id = {}", answerId);
		return answerRepository.findByQuestionIdAndId(questionId, answerId).map(answer -> {
			boolean isUserPermitted = PermissionsUtils.checkIfUserIsPermitted(answer, user);
			log.info("isUserPermitted = {}", isUserPermitted);
			if (isUserPermitted) {
				answerRepository.delete(answer);
				return true;
			} else {
				throw new ChangeNotAllowedException();
			}
		}).orElseThrow(() -> new ResourceNotFoundException(String.format(ANSWER_NOT_FOUND, answerId, questionId)));
	}

	public ResponseEntity<Object> generateAnswersFile(Long questionId, FileTypes filetype, Filter filter)
			throws IOException, DocumentException {
		QuestionGet question = questionManager.getQuestion(questionId);
		List<AnswerGet> answersList = searchAnswers(questionId, filter);
		String fileId = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
		String fileName = "Odpowiedzi_" + fileId;

		byte[] bytes;
		if (filetype.equals(FileTypes.EXCEL)) {
			fileName += ".xlsx";
			bytes = excelGenerator.generateAnswersExcelFile(answersList, question);
		} else {
			fileName += ".pdf";
			bytes = pdfGenerator.generateAnswersPdfFile(answersList, question);
		}
		return FileResponseGenerator.generateResponseWithFile(bytes, fileName);
	}

	private String generateEmailContent(QuestionEntity question, AnswerEntity answer, boolean isNewAnswer) {
		UserEntity questionUser = question.getUser();
		UserEntity answerUser = answer.getUser();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Witaj ").append(questionUser.getUsername()).append(",")
				.append("<br><br>Użytkownik <b>").append(answerUser.getUsername());
		if (isNewAnswer) {
			stringBuilder.append("</b> opublikował odpowiedź na Twoje pytanie:");
		} else {
			stringBuilder.append("</b> zaktualizował odpowiedź na Twoje pytanie:");
		}
		stringBuilder.append("<br><br><b>Tytuł:</b><br>").append(question.getTitle())
				.append("<br><br><b>Opis:</b><br>").append(question.getDescription())
				.append("<br><br><br><b>Treść odpowiedzi:</b><br>")
				.append(answer.getText().replace("\n", "<br>"));
		return stringBuilder.toString();
	}
}
