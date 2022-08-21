package pl.marcinm312.springdatasecurityex.shared.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.shared.exception.FileException;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static pl.marcinm312.springdatasecurityex.shared.file.Columns.*;

@Slf4j
@Component
public class ExcelGenerator {

	public byte[] generateAnswersExcelFile(List<AnswerGet> answersList, QuestionGet question) throws FileException {

		log.info("Starting generating answers Excel file for question = {}", question);
		log.info("answersList.size()={}", answersList.size());

		String[] columns = {ID_COLUMN, ANSWER_TEXT_COLUMN, CREATION_DATE_COLUMN, MODIFICATION_DATE_COLUMN, USER_COLUMN};

		try (Workbook workbook = new XSSFWorkbook();
			 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			CellStyle headerCellStyle = getHeaderCellStyle(workbook);
			CellStyle dateCellStyle = getDateCellStyle(workbook);

			Sheet answersSheet = workbook.createSheet("Odpowiedzi");

			Row headerRow = answersSheet.createRow(0);

			for (int i = 0; i < columns.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			int rowNum = 1;
			for (AnswerGet answer : answersList) {
				Row row = answersSheet.createRow(rowNum++);

				row.createCell(0).setCellValue(answer.getId());
				row.createCell(1).setCellValue(addValueWithNewLines(answer.getText()));

				createCellWithDate(dateCellStyle, answer.getCreatedAtAsString(), row, 2);
				createCellWithDate(dateCellStyle, answer.getUpdatedAtAsString(), row, 3);

				row.createCell(4).setCellValue(answer.getUser());
			}

			for (int i = 0; i < columns.length; i++) {
				answersSheet.autoSizeColumn(i);
			}

			Sheet questionSheet = workbook.createSheet("Pytanie");

			createRowWithTwoColumns(headerCellStyle, dateCellStyle, questionSheet, 0, ID_COLUMN,
					question.getId().toString(), false);

			createRowWithTwoColumns(headerCellStyle, dateCellStyle, questionSheet, 1, QUESTION_TITLE_COLUMN,
					question.getTitle(), false);

			createRowWithTwoColumns(headerCellStyle, dateCellStyle, questionSheet, 2, QUESTION_DESCRIPTION_COLUMN,
					addValueWithNewLines(question.getDescription()), false);

			createRowWithTwoColumns(headerCellStyle, dateCellStyle, questionSheet, 3, CREATION_DATE_COLUMN,
					question.getCreatedAtAsString(), true);

			createRowWithTwoColumns(headerCellStyle, dateCellStyle, questionSheet, 4, MODIFICATION_DATE_COLUMN,
					question.getUpdatedAtAsString(), true);

			createRowWithTwoColumns(headerCellStyle, dateCellStyle, questionSheet, 5, USER_COLUMN,
					question.getUser(), false);

			questionSheet.autoSizeColumn(0);
			questionSheet.autoSizeColumn(1);

			workbook.write(outputStream);

			log.info("Answers Excel file generated");
			return outputStream.toByteArray();

		} catch (Exception e) {
			throw new FileException(e.getMessage());
		}
	}

	public byte[] generateQuestionsExcelFile(List<QuestionGet> questionsList) throws FileException {

		log.info("Starting generating questions Excel file");
		log.info("questionsList.size()={}", questionsList.size());

		String[] columns = {ID_COLUMN, QUESTION_TITLE_COLUMN, QUESTION_DESCRIPTION_COLUMN, CREATION_DATE_COLUMN, MODIFICATION_DATE_COLUMN, USER_COLUMN};

		try (Workbook workbook = new XSSFWorkbook();
			 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			Sheet sheet = workbook.createSheet("Pytania");
			CellStyle headerCellStyle = getHeaderCellStyle(workbook);

			Row headerRow = sheet.createRow(0);

			for (int i = 0; i < columns.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			CellStyle dateCellStyle = getDateCellStyle(workbook);

			int rowNum = 1;
			for (QuestionGet question : questionsList) {
				Row row = sheet.createRow(rowNum++);

				row.createCell(0).setCellValue(question.getId());
				row.createCell(1).setCellValue(question.getTitle());
				row.createCell(2).setCellValue(addValueWithNewLines(question.getDescription()));

				createCellWithDate(dateCellStyle, question.getCreatedAtAsString(), row, 3);
				createCellWithDate(dateCellStyle, question.getUpdatedAtAsString(), row, 4);

				row.createCell(5).setCellValue(question.getUser());
			}

			for (int i = 0; i < columns.length; i++) {
				sheet.autoSizeColumn(i);
			}

			workbook.write(outputStream);

			log.info("Questions Excel file generated");
			return outputStream.toByteArray();

		} catch (Exception e) {
			throw new FileException(e.getMessage());
		}
	}

	private void createRowWithTwoColumns(CellStyle headerCellStyle, CellStyle dateCellStyle, Sheet sheet,
										 int rowNumber, String firstValue, String secondValue, boolean isDate) {

		Row row = sheet.createRow(rowNumber);
		Cell firstCell = row.createCell(0);
		firstCell.setCellValue(firstValue);
		firstCell.setCellStyle(headerCellStyle);
		if (isDate) {
			createCellWithDate(dateCellStyle, secondValue, row, 1);
		} else {
			row.createCell(1).setCellValue(secondValue);
		}
	}

	private void createCellWithDate(CellStyle dateCellStyle, String dateAsString, Row row, int column) {

		Cell cellWithDate = row.createCell(column);
		cellWithDate.setCellValue(dateAsString);
		cellWithDate.setCellStyle(dateCellStyle);
	}

	private CellStyle getDateCellStyle(Workbook workbook) {

		CreationHelper createHelper = workbook.getCreationHelper();
		CellStyle dateCellStyle = workbook.createCellStyle();
		dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd hh:mm:ss"));
		return dateCellStyle;
	}

	private CellStyle getHeaderCellStyle(Workbook workbook) {

		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 14);
		headerFont.setColor(IndexedColors.RED.getIndex());
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		return headerCellStyle;
	}

	private String addValueWithNewLines(String value) {

		if (value == null || value.isEmpty()) {
			return "";
		}
		return value.replace("\n", " ");
	}
}
