package pl.marcinm312.springdatasecurityex.service.file;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.marcinm312.springdatasecurityex.model.Question;
import pl.marcinm312.springdatasecurityex.testdataprovider.QuestionDataProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelGeneratorTest {

    ExcelGenerator excelGenerator;

    @BeforeEach
    public void setup() {
        excelGenerator = new ExcelGenerator();
    }

    @Test
    public void generateQuestionsExcelFile_simpleCase_success() throws IOException {
        List<Question> questionsList = QuestionDataProvider.prepareExampleQuestionsList();
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
        checkCellStringValue(sheet, evaluator, "F2", questionsList.get(0).getUser().getUsername());

        checkCellNumberValue(sheet, evaluator, "A3", questionsList.get(1).getId());
        checkCellStringValue(sheet, evaluator, "B3", questionsList.get(1).getTitle());
        checkCellStringValue(sheet, evaluator, "C3", questionsList.get(1).getDescription());
        checkCellStringValue(sheet, evaluator, "F3", questionsList.get(1).getUser().getUsername());

        checkCellNumberValue(sheet, evaluator, "A4", questionsList.get(2).getId());
        checkCellStringValue(sheet, evaluator, "B4", questionsList.get(2).getTitle());
        checkCellStringValue(sheet, evaluator, "C4", questionsList.get(2).getDescription());
        checkCellStringValue(sheet, evaluator, "F4", questionsList.get(2).getUser().getUsername());
    }

    @Test
    public void generateQuestionsExcelFile_emptyQuestionsList_success() throws IOException {
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
    }

    private void checkCellStringValue(Sheet sheet, FormulaEvaluator evaluator, String stringCellRef, String expectedValue) {
        CellReference cellReference = new CellReference(stringCellRef);
        Row row = sheet.getRow(cellReference.getRow());
        Cell cell = row.getCell(cellReference.getCol());

        CellValue cellValue = evaluator.evaluate(cell);
        String stringValue = cellValue.getStringValue();
        Assert.assertEquals(expectedValue, stringValue);
    }

    private void checkCellNumberValue(Sheet sheet, FormulaEvaluator evaluator, String stringCellRef, Long expectedValue) {
        CellReference cellReference = new CellReference(stringCellRef);
        Row row = sheet.getRow(cellReference.getRow());
        Cell cell = row.getCell(cellReference.getCol());

        CellValue cellValue = evaluator.evaluate(cell);
        Long numberValue = (long) cellValue.getNumberValue();
        Assert.assertEquals(expectedValue, numberValue);
    }
}