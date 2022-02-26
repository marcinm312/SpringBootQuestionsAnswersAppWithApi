package pl.marcinm312.springdatasecurityex.shared.file;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.marcinm312.springdatasecurityex.answer.model.AnswerEntity;
import pl.marcinm312.springdatasecurityex.answer.model.AnswerMapper;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.answer.testdataprovider.AnswerDataProvider;
import pl.marcinm312.springdatasecurityex.question.model.QuestionEntity;
import pl.marcinm312.springdatasecurityex.question.model.QuestionMapper;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.question.testdataprovider.QuestionDataProvider;

import java.io.ByteArrayInputStream;
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
		List<QuestionEntity> oldQuestionsList = QuestionDataProvider.prepareExampleQuestionsList();
		List<QuestionGet> questionsList = QuestionMapper.convertQuestionEntityListToQuestionGetList(oldQuestionsList);

		byte[] questionsExcelFile = excelGenerator.generateQuestionsExcelFile(questionsList);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(questionsExcelFile);

		Workbook wb = new XSSFWorkbook(inputStream);
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
	}

	@Test
	void generateQuestionsExcelFile_emptyQuestionsList_success() throws IOException {
		byte[] questionsExcelFile = excelGenerator.generateQuestionsExcelFile(new ArrayList<>());
		ByteArrayInputStream inputStream = new ByteArrayInputStream(questionsExcelFile);

		Workbook wb = new XSSFWorkbook(inputStream);
		Sheet sheet = wb.getSheetAt(0);
		FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

		checkCellStringValue(sheet, evaluator, "A1", "Id");
		checkCellStringValue(sheet, evaluator, "B1", "Tytuł");
		checkCellStringValue(sheet, evaluator, "C1", "Opis");
		checkCellStringValue(sheet, evaluator, "D1", "Data utworzenia");
		checkCellStringValue(sheet, evaluator, "E1", "Data modyfikacji");
		checkCellStringValue(sheet, evaluator, "F1", "Użytkownik");
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
		List<AnswerEntity> oldAnswersList = AnswerDataProvider.prepareExampleAnswersList();
		List<AnswerGet> answersList = AnswerMapper.convertAnswerEntityListToAnswerGetList(oldAnswersList);
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet questionGet = QuestionMapper.convertQuestionEntityToQuestionGet(question);

		byte[] answersExcelFile = excelGenerator.generateAnswersExcelFile(answersList, questionGet);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(answersExcelFile);

		Workbook wb = new XSSFWorkbook(inputStream);
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
	}

	@Test
	void generateAnswersExcelFile_emptyAnswersList_success() throws IOException {
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet questionGet = QuestionMapper.convertQuestionEntityToQuestionGet(question);

		byte[] answersExcelFile = excelGenerator.generateAnswersExcelFile(new ArrayList<>(), questionGet);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(answersExcelFile);

		Workbook wb = new XSSFWorkbook(inputStream);
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
	}
}