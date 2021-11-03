package pl.marcinm312.springdatasecurityex.shared.file;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.marcinm312.springdatasecurityex.answer.model.Answer;
import pl.marcinm312.springdatasecurityex.answer.model.AnswerMapper;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.question.model.Question;
import pl.marcinm312.springdatasecurityex.question.model.QuestionMapper;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.answer.testdataprovider.AnswerDataProvider;
import pl.marcinm312.springdatasecurityex.question.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springdatasecurityex.shared.file.ExcelGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ExcelGeneratorTest {

	private ExcelGenerator excelGenerator;

	@BeforeEach
	void setup() {
		excelGenerator = new ExcelGenerator();
	}

	@Test
	void generateQuestionsExcelFile_simpleCase_success() throws IOException {
		List<Question> oldQuestionsList = QuestionDataProvider.prepareExampleQuestionsList();
		List<QuestionGet> questionsList = QuestionMapper.convertQuestionListToQuestionGetList(oldQuestionsList);

		File questionsExcelFile = excelGenerator.generateQuestionsExcelFile(questionsList);

		FileInputStream fis = new FileInputStream(questionsExcelFile);
		Workbook wb = new XSSFWorkbook(fis);
		Sheet sheet = wb.getSheetAt(0);
		FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

		checkCellStringValue(sheet, evaluator, "A1", "Id");
		checkCellStringValue(sheet, evaluator, "B1", "Tytuł");
		checkCellStringValue(sheet, evaluator, "C1", "Opis");
		checkCellStringValue(sheet, evaluator, "D1", "Data utworzenia");
		checkCellStringValue(sheet, evaluator, "E1", "Data modyfikacji");
		checkCellStringValue(sheet, evaluator, "F1", "Użytkownik");

		checkCellNumberValue(sheet, evaluator, "A2", questionsList.get(0).getId());
		checkCellStringValue(sheet, evaluator, "B2", questionsList.get(0).getTitle());
		checkCellStringValue(sheet, evaluator, "C2", questionsList.get(0).getDescription());
		checkCellStringValue(sheet, evaluator, "F2", questionsList.get(0).getUser());

		checkCellNumberValue(sheet, evaluator, "A3", questionsList.get(1).getId());
		checkCellStringValue(sheet, evaluator, "B3", questionsList.get(1).getTitle());
		checkCellStringValue(sheet, evaluator, "C3", questionsList.get(1).getDescription());
		checkCellStringValue(sheet, evaluator, "F3", questionsList.get(1).getUser());

		checkCellNumberValue(sheet, evaluator, "A4", questionsList.get(2).getId());
		checkCellStringValue(sheet, evaluator, "B4", questionsList.get(2).getTitle());
		checkCellStringValue(sheet, evaluator, "C4", questionsList.get(2).getDescription());
		checkCellStringValue(sheet, evaluator, "F4", questionsList.get(2).getUser());

		Assertions.assertTrue(questionsExcelFile.getName().startsWith("Pytania"));
		Assertions.assertTrue(questionsExcelFile.getName().endsWith(".xlsx"));
	}

	@Test
	void generateQuestionsExcelFile_emptyQuestionsList_success() throws IOException {
		File questionsExcelFile = excelGenerator.generateQuestionsExcelFile(new ArrayList<>());

		FileInputStream fis = new FileInputStream(questionsExcelFile);
		Workbook wb = new XSSFWorkbook(fis);
		Sheet sheet = wb.getSheetAt(0);
		FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

		checkCellStringValue(sheet, evaluator, "A1", "Id");
		checkCellStringValue(sheet, evaluator, "B1", "Tytuł");
		checkCellStringValue(sheet, evaluator, "C1", "Opis");
		checkCellStringValue(sheet, evaluator, "D1", "Data utworzenia");
		checkCellStringValue(sheet, evaluator, "E1", "Data modyfikacji");
		checkCellStringValue(sheet, evaluator, "F1", "Użytkownik");

		Assertions.assertTrue(questionsExcelFile.getName().startsWith("Pytania"));
		Assertions.assertTrue(questionsExcelFile.getName().endsWith(".xlsx"));
	}

	private void checkCellStringValue(Sheet sheet, FormulaEvaluator evaluator, String stringCellRef, String expectedValue) {
		CellReference cellReference = new CellReference(stringCellRef);
		Row row = sheet.getRow(cellReference.getRow());
		Cell cell = row.getCell(cellReference.getCol());

		CellValue cellValue = evaluator.evaluate(cell);
		String stringValue = cellValue.getStringValue();
		Assertions.assertEquals(expectedValue, stringValue);
	}

	private void checkCellNumberValue(Sheet sheet, FormulaEvaluator evaluator, String stringCellRef, Long expectedValue) {
		CellReference cellReference = new CellReference(stringCellRef);
		Row row = sheet.getRow(cellReference.getRow());
		Cell cell = row.getCell(cellReference.getCol());

		CellValue cellValue = evaluator.evaluate(cell);
		Long numberValue = (long) cellValue.getNumberValue();
		Assertions.assertEquals(expectedValue, numberValue);
	}

	@Test
	void generateAnswersExcelFile_simpleCase_success() throws IOException {
		List<Answer> oldAnswersList = AnswerDataProvider.prepareExampleAnswersList();
		List<AnswerGet> answersList = AnswerMapper.convertAnswerListToAnswerGetList(oldAnswersList);
		Question question = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet questionGet = QuestionMapper.convertQuestionToQuestionGet(question);

		File answersExcelFile = excelGenerator.generateAnswersExcelFile(answersList, questionGet);

		FileInputStream fis = new FileInputStream(answersExcelFile);
		Workbook wb = new XSSFWorkbook(fis);
		FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

		Sheet sheet0 = wb.getSheetAt(0);

		checkCellStringValue(sheet0, evaluator, "A1", "Id");
		checkCellStringValue(sheet0, evaluator, "B1", "Treść odpowiedzi");
		checkCellStringValue(sheet0, evaluator, "C1", "Data utworzenia");
		checkCellStringValue(sheet0, evaluator, "D1", "Data modyfikacji");
		checkCellStringValue(sheet0, evaluator, "E1", "Użytkownik");

		checkCellNumberValue(sheet0, evaluator, "A2", answersList.get(0).getId());
		checkCellStringValue(sheet0, evaluator, "B2", answersList.get(0).getText());
		checkCellStringValue(sheet0, evaluator, "E2", answersList.get(0).getUser());

		checkCellNumberValue(sheet0, evaluator, "A4", answersList.get(2).getId());
		checkCellStringValue(sheet0, evaluator, "B4", answersList.get(2).getText());
		checkCellStringValue(sheet0, evaluator, "E4", answersList.get(2).getUser());

		Sheet sheet1 = wb.getSheetAt(1);

		checkCellStringValue(sheet1, evaluator, "A1", "Id");
		checkCellStringValue(sheet1, evaluator, "A2", "Tytuł");
		checkCellStringValue(sheet1, evaluator, "A3", "Opis");
		checkCellStringValue(sheet1, evaluator, "A4", "Data utworzenia");
		checkCellStringValue(sheet1, evaluator, "A5", "Data modyfikacji");
		checkCellStringValue(sheet1, evaluator, "A6", "Użytkownik");

		checkCellStringValue(sheet1, evaluator, "B1", questionGet.getId().toString());
		checkCellStringValue(sheet1, evaluator, "B2", questionGet.getTitle());
		checkCellStringValue(sheet1, evaluator, "B3", questionGet.getDescription());
		checkCellStringValue(sheet1, evaluator, "B6", questionGet.getUser());

		Assertions.assertTrue(answersExcelFile.getName().startsWith("Odpowiedzi"));
		Assertions.assertTrue(answersExcelFile.getName().endsWith(".xlsx"));
	}

	@Test
	void generateAnswersExcelFile_emptyAnswersList_success() throws IOException {
		Question question = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet questionGet = QuestionMapper.convertQuestionToQuestionGet(question);

		File answersExcelFile = excelGenerator.generateAnswersExcelFile(new ArrayList<>(), questionGet);

		FileInputStream fis = new FileInputStream(answersExcelFile);
		Workbook wb = new XSSFWorkbook(fis);
		FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

		Sheet sheet0 = wb.getSheetAt(0);

		checkCellStringValue(sheet0, evaluator, "A1", "Id");
		checkCellStringValue(sheet0, evaluator, "B1", "Treść odpowiedzi");
		checkCellStringValue(sheet0, evaluator, "C1", "Data utworzenia");
		checkCellStringValue(sheet0, evaluator, "D1", "Data modyfikacji");
		checkCellStringValue(sheet0, evaluator, "E1", "Użytkownik");

		Sheet sheet1 = wb.getSheetAt(1);

		checkCellStringValue(sheet1, evaluator, "A1", "Id");
		checkCellStringValue(sheet1, evaluator, "A2", "Tytuł");
		checkCellStringValue(sheet1, evaluator, "A3", "Opis");
		checkCellStringValue(sheet1, evaluator, "A4", "Data utworzenia");
		checkCellStringValue(sheet1, evaluator, "A5", "Data modyfikacji");
		checkCellStringValue(sheet1, evaluator, "A6", "Użytkownik");

		checkCellStringValue(sheet1, evaluator, "B1", questionGet.getId().toString());
		checkCellStringValue(sheet1, evaluator, "B2", questionGet.getTitle());
		checkCellStringValue(sheet1, evaluator, "B3", questionGet.getDescription());
		checkCellStringValue(sheet1, evaluator, "B6", questionGet.getUser());

		Assertions.assertTrue(answersExcelFile.getName().startsWith("Odpowiedzi"));
		Assertions.assertTrue(answersExcelFile.getName().endsWith(".xlsx"));
	}
}