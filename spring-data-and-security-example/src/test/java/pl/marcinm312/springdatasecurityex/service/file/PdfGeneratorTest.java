package pl.marcinm312.springdatasecurityex.service.file;

import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PdfGeneratorTest {

	PdfGenerator pdfGenerator;

	@BeforeEach
	void setup() {
		pdfGenerator = new PdfGenerator();
	}

	@Test
	void generateQuestionsPdfFile_simpleCase_success() throws IOException, DocumentException {
		List<Question> questionsList = QuestionDataProvider.prepareExampleQuestionsList();
		Assertions.assertDoesNotThrow(() -> pdfGenerator.generateQuestionsPdfFile(questionsList));
		File questionsPdfFile = pdfGenerator.generateQuestionsPdfFile(questionsList);
		Assertions.assertEquals("Pytania.pdf", questionsPdfFile.getName());
	}

	@Test
	void generateQuestionsPdfFile_emptyQuestionsList_success() throws IOException, DocumentException {
		Assertions.assertDoesNotThrow(() -> pdfGenerator.generateQuestionsPdfFile(new ArrayList<>()));
		File questionsPdfFile = pdfGenerator.generateQuestionsPdfFile(new ArrayList<>());
		Assertions.assertEquals("Pytania.pdf", questionsPdfFile.getName());
	}
}