package pl.marcinm312.springdatasecurityex.shared.file;

import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.marcinm312.springdatasecurityex.answer.model.AnswerEntity;
import pl.marcinm312.springdatasecurityex.answer.model.AnswerMapper;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.question.model.QuestionMapper;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.answer.testdataprovider.AnswerDataProvider;
import pl.marcinm312.springdatasecurityex.question.testdataprovider.QuestionDataProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PdfGeneratorTest {

	private PdfGenerator pdfGenerator;

	@BeforeEach
	void setup() throws DocumentException, IOException {
		pdfGenerator = new PdfGenerator();
	}

	@Test
	void generateQuestionsPdfFile_simpleCase_success() throws IOException, DocumentException {
		List<QuestionEntity> oldQuestionsList = QuestionDataProvider.prepareExampleQuestionsList();
		List<QuestionGet> questionsList = QuestionMapper.convertQuestionEntityListToQuestionGetList(oldQuestionsList);

		Assertions.assertDoesNotThrow(() -> pdfGenerator.generateQuestionsPdfFile(questionsList));
		File questionsPdfFile = pdfGenerator.generateQuestionsPdfFile(questionsList);

		Assertions.assertTrue(questionsPdfFile.getName().startsWith("Pytania"));
		Assertions.assertTrue(questionsPdfFile.getName().endsWith(".pdf"));
	}

	@Test
	void generateQuestionsPdfFile_emptyQuestionsList_success() throws IOException, DocumentException {
		Assertions.assertDoesNotThrow(() -> pdfGenerator.generateQuestionsPdfFile(new ArrayList<>()));
		File questionsPdfFile = pdfGenerator.generateQuestionsPdfFile(new ArrayList<>());

		Assertions.assertTrue(questionsPdfFile.getName().startsWith("Pytania"));
		Assertions.assertTrue(questionsPdfFile.getName().endsWith(".pdf"));
	}

	@Test
	void generateAnswersPdfFile_simpleCase_success() throws DocumentException, IOException {
		List<AnswerEntity> oldAnswersList = AnswerDataProvider.prepareExampleAnswersList();
		List<AnswerGet> answersList = AnswerMapper.convertAnswerEntityListToAnswerGetList(oldAnswersList);
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet questionGet = QuestionMapper.convertQuestionEntityToQuestionGet(question);

		Assertions.assertDoesNotThrow(() -> pdfGenerator.generateAnswersPdfFile(answersList, questionGet));
		File answersPdfFile = pdfGenerator.generateAnswersPdfFile(answersList, questionGet);

		Assertions.assertTrue(answersPdfFile.getName().startsWith("Odpowiedzi"));
		Assertions.assertTrue(answersPdfFile.getName().endsWith(".pdf"));
	}

	@Test
	void generateAnswersPdfFile_emptyAnswersList_success() throws DocumentException, IOException {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet questionGet = QuestionMapper.convertQuestionEntityToQuestionGet(question);

		Assertions.assertDoesNotThrow(() -> pdfGenerator.generateAnswersPdfFile(new ArrayList<>(), questionGet));
		File answersPdfFile = pdfGenerator.generateAnswersPdfFile(new ArrayList<>(), questionGet);

		Assertions.assertTrue(answersPdfFile.getName().startsWith("Odpowiedzi"));
		Assertions.assertTrue(answersPdfFile.getName().endsWith(".pdf"));
	}
}