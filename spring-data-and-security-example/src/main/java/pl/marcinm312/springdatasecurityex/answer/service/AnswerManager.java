package pl.marcinm312.springdatasecurityex.answer.service;

import com.itextpdf.text.DocumentException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.marcinm312.springdatasecurityex.answer.model.AnswerEntity;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.shared.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.shared.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.shared.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.answer.model.AnswerMapper;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerCreateUpdate;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.user.model.User;
import pl.marcinm312.springdatasecurityex.answer.repository.AnswerRepository;
import pl.marcinm312.springdatasecurityex.shared.mail.MailService;
import pl.marcinm312.springdatasecurityex.question.service.QuestionManager;
import pl.marcinm312.springdatasecurityex.config.security.utils.PermissionsUtils;
import pl.marcinm312.springdatasecurityex.shared.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.shared.file.FileResponseGenerator;
import pl.marcinm312.springdatasecurityex.shared.file.PdfGenerator;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class AnswerManager {

	private static final String QUESTION_NOT_FOUND = "Question not found with id: ";
	private static final String ANSWER_NOT_FOUND = "Answer not found with questionId: %d and answerId: %d";

	private final AnswerRepository answerRepository;
	private final QuestionManager questionManager;
	private final MailService mailService;
	private final ExcelGenerator excelGenerator;
	private final PdfGenerator pdfGenerator;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	public AnswerManager(AnswerRepository answerRepository, QuestionManager questionManager,
			MailService mailService, ExcelGenerator excelGenerator, PdfGenerator pdfGenerator) {
		this.answerRepository = answerRepository;
		this.questionManager = questionManager;
		this.mailService = mailService;
		this.excelGenerator = excelGenerator;
		this.pdfGenerator = pdfGenerator;
	}

	public List<AnswerGet> getAnswersByQuestionId(Long questionId) {
		questionManager.getQuestion(questionId);
		List<AnswerEntity> answersFromDB = answerRepository.findByQuestionIdOrderByIdDesc(questionId);
		return AnswerMapper.convertAnswerEntityListToAnswerGetList(answersFromDB);
	}

	public AnswerGet getAnswerByQuestionIdAndAnswerId(Long questionId, Long answerId) {
		AnswerEntity answerFromDB = answerRepository.findByQuestionIdAndId(questionId, answerId)
				.orElseThrow(() -> new ResourceNotFoundException(String.format(ANSWER_NOT_FOUND, questionId, answerId)));
		return AnswerMapper.convertAnswerEntityToAnswerGet(answerFromDB);
	}

	@Transactional
	public AnswerGet addAnswer(Long questionId, AnswerCreateUpdate answerRequest, User user) {
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
	public AnswerGet updateAnswer(Long questionId, Long answerId, AnswerCreateUpdate answerRequest, User user) {
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
		}).orElseThrow(() -> new ResourceNotFoundException(String.format(ANSWER_NOT_FOUND, questionId, answerId)));
	}

	public boolean deleteAnswer(Long questionId, Long answerId, User user) {
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
		}).orElseThrow(() -> new ResourceNotFoundException(String.format(ANSWER_NOT_FOUND, questionId, answerId)));
	}

	public ResponseEntity<Object> generateAnswersFile(Long questionId, FileTypes filetype)
			throws IOException, DocumentException {
		QuestionGet question;
		List<AnswerGet> answersList;
		try {
			question = questionManager.getQuestion(questionId);
			answersList = getAnswersByQuestionId(questionId);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
		File file;
		if (filetype.equals(FileTypes.EXCEL)) {
			file = excelGenerator.generateAnswersExcelFile(answersList, question);
		} else {
			file = pdfGenerator.generateAnswersPdfFile(answersList, question);
		}
		return FileResponseGenerator.generateResponseWithFile(file);
	}

	private String generateEmailContent(QuestionEntity question, AnswerEntity answer, boolean isNewAnswer) {
		User questionUser = question.getUser();
		User answerUser = answer.getUser();
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
