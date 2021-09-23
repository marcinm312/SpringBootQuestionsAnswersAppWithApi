package pl.marcinm312.springdatasecurityex.service.db;

import com.itextpdf.text.DocumentException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.marcinm312.springdatasecurityex.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.exception.ChangeNotAllowedException;
import pl.marcinm312.springdatasecurityex.exception.ResourceNotFoundException;
import pl.marcinm312.springdatasecurityex.model.answer.Answer;
import pl.marcinm312.springdatasecurityex.model.answer.AnswerMapper;
import pl.marcinm312.springdatasecurityex.model.answer.dto.AnswerCreateUpdate;
import pl.marcinm312.springdatasecurityex.model.answer.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.model.question.Question;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.model.user.User;
import pl.marcinm312.springdatasecurityex.repository.AnswerRepository;
import pl.marcinm312.springdatasecurityex.service.MailService;
import pl.marcinm312.springdatasecurityex.utils.PermissionsUtils;
import pl.marcinm312.springdatasecurityex.utils.file.ExcelGenerator;
import pl.marcinm312.springdatasecurityex.utils.file.FileResponseGenerator;
import pl.marcinm312.springdatasecurityex.utils.file.PdfGenerator;

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
		List<Answer> answersFromDB = answerRepository.findByQuestionIdOrderByIdDesc(questionId);
		return AnswerMapper.convertAnswerListToAnswerGetList(answersFromDB);
	}

	public AnswerGet getAnswerByQuestionIdAndAnswerId(Long questionId, Long answerId) {
		Answer answerFromDB = answerRepository.findByQuestionIdAndId(questionId, answerId)
				.orElseThrow(() -> new ResourceNotFoundException(String.format(ANSWER_NOT_FOUND, questionId, answerId)));
		return AnswerMapper.convertAnswerToAnswerGet(answerFromDB);
	}

	@Transactional
	public AnswerGet addAnswer(Long questionId, AnswerCreateUpdate answerRequest, User user) {
		return questionManager.getQuestionEntity(questionId).map(question -> {
			Answer answer = new Answer(answerRequest.getText());
			answer.setQuestion(question);
			answer.setUser(user);
			log.info("Adding answer = {}", answer);
			Answer savedAnswer = answerRepository.save(answer);
			try {
				String email = question.getUser().getEmail();
				String subject = "Opublikowano odpowiedź na Twoje pytanie o id: " + question.getId();
				String content = generateEmailContent(question, savedAnswer, true);
				mailService.sendMail(email, subject, content, true);
			} catch (MessagingException e) {
				log.error("An error occurred while sending the email. [MESSAGE]: {}", e.getMessage());
			}
			return AnswerMapper.convertAnswerToAnswerGet(savedAnswer);

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
				Answer savedAnswer = answerRepository.save(answer);
				try {
					Question question = answer.getQuestion();
					String email = question.getUser().getEmail();
					String subject = "Zaktualizowano odpowiedź na Twoje pytanie o id: " + question.getId();
					String content = generateEmailContent(question, savedAnswer, false);
					mailService.sendMail(email, subject, content, true);
				} catch (MessagingException e) {
					log.error("An error occurred while sending the email. [MESSAGE]: {}", e.getMessage());
				}
				return AnswerMapper.convertAnswerToAnswerGet(savedAnswer);
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

	private String generateEmailContent(Question question, Answer answer, boolean isNewAnswer) {
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
