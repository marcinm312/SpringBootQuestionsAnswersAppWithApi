package pl.marcinm312.springdatasecurityex.service.file;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;

import java.util.ArrayList;
import java.util.List;

class PdfGeneratorTest {

	PdfGenerator pdfGenerator;

	@BeforeEach
	void setup() {
		pdfGenerator = new PdfGenerator();
	}

	@Test
	void generateQuestionsPdfFile_simpleCase_success() {
		List<Question> questionsList = QuestionDataProvider.prepareExampleQuestionsList();
		Assertions.assertAll(() -> pdfGenerator.generateQuestionsPdfFile(questionsList));
		Assertions.assertDoesNotThrow(() -> pdfGenerator.generateQuestionsPdfFile(questionsList));
	}

	@Test
	void generateQuestionsPdfFile_emptyQuestionsList_success() {
		Assertions.assertAll(() -> pdfGenerator.generateQuestionsPdfFile(new ArrayList<>()));
		Assertions.assertDoesNotThrow(() -> pdfGenerator.generateQuestionsPdfFile(new ArrayList<>()));
	}
}