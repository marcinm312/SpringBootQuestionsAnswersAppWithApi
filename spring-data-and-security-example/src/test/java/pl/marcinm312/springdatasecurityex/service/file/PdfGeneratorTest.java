package pl.marcinm312.springdatasecurityex.service.file;

import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.marcinm312.springdatasecurityex.model.answer.Answer;
import pl.marcinm312.springdatasecurityex.model.answer.AnswerMapper;
import pl.marcinm312.springdatasecurityex.model.answer.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.model.question.Question;
import pl.marcinm312.springdatasecurityex.model.question.QuestionMapper;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.testdataprovider.AnswerDataProvider;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PdfGeneratorTest {

	@Test
	void generateQuestionsPdfFile_simpleCase_success() throws IOException, DocumentException {
		List<Question> oldQuestionsList = QuestionDataProvider.prepareExampleQuestionsList();
		List<QuestionGet> questionsList = QuestionMapper.convertQuestionListToQuestionGetList(oldQuestionsList);

		Assertions.assertDoesNotThrow(() -> PdfGenerator.generateQuestionsPdfFile(questionsList));
		File questionsPdfFile = PdfGenerator.generateQuestionsPdfFile(questionsList);

		Assertions.assertTrue(questionsPdfFile.getName().startsWith("Pytania"));
		Assertions.assertTrue(questionsPdfFile.getName().endsWith(".pdf"));
	}

	@Test
	void generateQuestionsPdfFile_emptyQuestionsList_success() throws IOException, DocumentException {
		Assertions.assertDoesNotThrow(() -> PdfGenerator.generateQuestionsPdfFile(new ArrayList<>()));
		File questionsPdfFile = PdfGenerator.generateQuestionsPdfFile(new ArrayList<>());

		Assertions.assertTrue(questionsPdfFile.getName().startsWith("Pytania"));
		Assertions.assertTrue(questionsPdfFile.getName().endsWith(".pdf"));
	}

	@Test
	void generateAnswersPdfFile_simpleCase_success() throws DocumentException, IOException {
		List<Answer> oldAnswersList = AnswerDataProvider.prepareExampleAnswersList();
		List<AnswerGet> answersList = AnswerMapper.convertAnswerListToAnswerGetList(oldAnswersList);
		Question question = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet questionGet = QuestionMapper.convertQuestionToQuestionGet(question);

		Assertions.assertDoesNotThrow(() -> PdfGenerator.generateAnswersPdfFile(answersList, questionGet));
		File answersPdfFile = PdfGenerator.generateAnswersPdfFile(answersList, questionGet);

		Assertions.assertTrue(answersPdfFile.getName().startsWith("Odpowiedzi"));
		Assertions.assertTrue(answersPdfFile.getName().endsWith(".pdf"));
	}

	@Test
	void generateAnswersPdfFile_emptyAnswersList_success() throws DocumentException, IOException {
		Question question = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet questionGet = QuestionMapper.convertQuestionToQuestionGet(question);

		Assertions.assertDoesNotThrow(() -> PdfGenerator.generateAnswersPdfFile(new ArrayList<>(), questionGet));
		File answersPdfFile = PdfGenerator.generateAnswersPdfFile(new ArrayList<>(), questionGet);

		Assertions.assertTrue(answersPdfFile.getName().startsWith("Odpowiedzi"));
		Assertions.assertTrue(answersPdfFile.getName().endsWith(".pdf"));
	}
}