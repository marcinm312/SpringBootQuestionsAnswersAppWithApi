package pl.marcinm312.springquestionsanswers.shared.file;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.marcinm312.springquestionsanswers.answer.model.AnswerEntity;
import pl.marcinm312.springquestionsanswers.answer.model.AnswerMapper;
import pl.marcinm312.springquestionsanswers.answer.model.dto.AnswerGet;
import pl.marcinm312.springquestionsanswers.answer.testdataprovider.AnswerDataProvider;
import pl.marcinm312.springquestionsanswers.question.model.QuestionEntity;
import pl.marcinm312.springquestionsanswers.question.model.QuestionMapper;
import pl.marcinm312.springquestionsanswers.question.model.dto.QuestionGet;
import pl.marcinm312.springquestionsanswers.question.testdataprovider.QuestionDataProvider;
import pl.marcinm312.springquestionsanswers.shared.exception.FileException;

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
	void generateQuestionsExcelFile_simpleCase_success() throws IOException, FileException {

		List<QuestionEntity> oldQuestionsList = QuestionDataProvider.prepareExampleQuestionsList();
		List<QuestionGet> questionsList = QuestionMapper.convertQuestionEntityListToQuestionGetList(oldQuestionsList);

		byte[] questionsExcelFile = excelGenerator.generateQuestionsExcelFile(questionsList);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(questionsExcelFile);
		Workbook wb = new XSSFWorkbook(inputStream);
		Sheet sheet = wb.getSheetAt(0);

		checkCellStringValue(sheet, "A1", "Id");
		checkCellStringValue(sheet, "B1", "Tytuł");
		checkCellStringValue(sheet, "C1", "Opis");
		checkCellStringValue(sheet, "D1", "Data utworzenia");
		checkCellStringValue(sheet, "E1", "Data modyfikacji");
		checkCellStringValue(sheet, "F1", "Użytkownik");

		checkCellNumberValue(sheet, "A2", questionsList.get(0).getId());
		checkCellStringValue(sheet, "B2", questionsList.get(0).getTitle());
		checkCellStringValue(sheet, "C2", questionsList.get(0).getDescription());
		checkCellStringValue(sheet, "F2", questionsList.get(0).getUser());

		checkCellNumberValue(sheet, "A3", questionsList.get(1).getId());
		checkCellStringValue(sheet, "B3", questionsList.get(1).getTitle());
		checkCellStringValue(sheet, "C3", questionsList.get(1).getDescription());
		checkCellStringValue(sheet, "F3", questionsList.get(1).getUser());

		checkCellNumberValue(sheet, "A4", questionsList.get(2).getId());
		checkCellStringValue(sheet, "B4", questionsList.get(2).getTitle());
		checkCellStringValue(sheet, "C4", questionsList.get(2).getDescription());
		checkCellStringValue(sheet, "F4", questionsList.get(2).getUser());
	}

	@Test
	void generateQuestionsExcelFile_emptyQuestionsList_success() throws IOException, FileException {

		byte[] questionsExcelFile = excelGenerator.generateQuestionsExcelFile(new ArrayList<>());

		ByteArrayInputStream inputStream = new ByteArrayInputStream(questionsExcelFile);
		Workbook wb = new XSSFWorkbook(inputStream);
		Sheet sheet = wb.getSheetAt(0);

		checkCellStringValue(sheet, "A1", "Id");
		checkCellStringValue(sheet, "B1", "Tytuł");
		checkCellStringValue(sheet, "C1", "Opis");
		checkCellStringValue(sheet, "D1", "Data utworzenia");
		checkCellStringValue(sheet, "E1", "Data modyfikacji");
		checkCellStringValue(sheet, "F1", "Użytkownik");
	}

	private void checkCellStringValue(Sheet sheet, String stringCellRef, String expectedValue) {

		CellReference cellReference = new CellReference(stringCellRef);
		Row row = sheet.getRow(cellReference.getRow());
		Cell cell = row.getCell(cellReference.getCol());

		String stringValue = cell.getStringCellValue();
		Assertions.assertEquals(expectedValue, stringValue);
	}

	private void checkCellNumberValue(Sheet sheet, String stringCellRef, Long expectedValue) {

		CellReference cellReference = new CellReference(stringCellRef);
		Row row = sheet.getRow(cellReference.getRow());
		Cell cell = row.getCell(cellReference.getCol());

		Long numberValue = (long) cell.getNumericCellValue();
		Assertions.assertEquals(expectedValue, numberValue);
	}

	@Test
	void generateAnswersExcelFile_simpleCase_success() throws IOException, FileException {

		List<AnswerEntity> oldAnswersList = AnswerDataProvider.prepareExampleAnswersList();
		List<AnswerGet> answersList = AnswerMapper.convertAnswerEntityListToAnswerGetList(oldAnswersList);
		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet questionGet = QuestionMapper.convertQuestionEntityToQuestionGet(question, false);

		byte[] answersExcelFile = excelGenerator.generateAnswersExcelFile(answersList, questionGet);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(answersExcelFile);
		Workbook wb = new XSSFWorkbook(inputStream);
		Sheet sheet0 = wb.getSheetAt(0);

		checkCellStringValue(sheet0, "A1", "Id");
		checkCellStringValue(sheet0, "B1", "Treść odpowiedzi");
		checkCellStringValue(sheet0, "C1", "Data utworzenia");
		checkCellStringValue(sheet0, "D1", "Data modyfikacji");
		checkCellStringValue(sheet0, "E1", "Użytkownik");

		checkCellNumberValue(sheet0, "A2", answersList.get(0).getId());
		checkCellStringValue(sheet0, "B2", answersList.get(0).getText());
		checkCellStringValue(sheet0, "E2", answersList.get(0).getUser());

		checkCellNumberValue(sheet0, "A4", answersList.get(2).getId());
		checkCellStringValue(sheet0, "B4", answersList.get(2).getText());
		checkCellStringValue(sheet0, "E4", answersList.get(2).getUser());

		Sheet sheet1 = wb.getSheetAt(1);

		checkCellStringValue(sheet1, "A1", "Id");
		checkCellStringValue(sheet1, "A2", "Tytuł");
		checkCellStringValue(sheet1, "A3", "Opis");
		checkCellStringValue(sheet1, "A4", "Data utworzenia");
		checkCellStringValue(sheet1, "A5", "Data modyfikacji");
		checkCellStringValue(sheet1, "A6", "Użytkownik");

		checkCellStringValue(sheet1, "B1", questionGet.getId().toString());
		checkCellStringValue(sheet1, "B2", questionGet.getTitle());
		checkCellStringValue(sheet1, "B3", questionGet.getDescription());
		checkCellStringValue(sheet1, "B6", questionGet.getUser());
	}

	@Test
	void generateAnswersExcelFile_emptyAnswersList_success() throws IOException, FileException {

		QuestionEntity question = QuestionDataProvider.prepareExampleQuestion();
		QuestionGet questionGet = QuestionMapper.convertQuestionEntityToQuestionGet(question, false);

		byte[] answersExcelFile = excelGenerator.generateAnswersExcelFile(new ArrayList<>(), questionGet);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(answersExcelFile);
		Workbook wb = new XSSFWorkbook(inputStream);
		Sheet sheet0 = wb.getSheetAt(0);

		checkCellStringValue(sheet0, "A1", "Id");
		checkCellStringValue(sheet0, "B1", "Treść odpowiedzi");
		checkCellStringValue(sheet0, "C1", "Data utworzenia");
		checkCellStringValue(sheet0, "D1", "Data modyfikacji");
		checkCellStringValue(sheet0, "E1", "Użytkownik");

		Sheet sheet1 = wb.getSheetAt(1);

		checkCellStringValue(sheet1, "A1", "Id");
		checkCellStringValue(sheet1, "A2", "Tytuł");
		checkCellStringValue(sheet1, "A3", "Opis");
		checkCellStringValue(sheet1, "A4", "Data utworzenia");
		checkCellStringValue(sheet1, "A5", "Data modyfikacji");
		checkCellStringValue(sheet1, "A6", "Użytkownik");

		checkCellStringValue(sheet1, "B1", questionGet.getId().toString());
		checkCellStringValue(sheet1, "B2", questionGet.getTitle());
		checkCellStringValue(sheet1, "B3", questionGet.getDescription());
		checkCellStringValue(sheet1, "B6", questionGet.getUser());
	}
}
