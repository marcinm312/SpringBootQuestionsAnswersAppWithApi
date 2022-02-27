package pl.marcinm312.springdatasecurityex.shared.file;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class ExcelGenerator {

	private static final String ID_COLUMN = "Id";
	private static final String TRESC_ODPOWIEDZI_COLUMN = "Treść odpowiedzi";
	private static final String DATA_UTWORZENIA_COLUMN = "Data utworzenia";
	private static final String DATA_MODYFIKACJI_COLUMN = "Data modyfikacji";
	private static final String UZYTKOWNIK_COLUMN = "Użytkownik";
	private static final String TYTUL_COLUMN = "Tytuł";
	private static final String OPIS_COLUMN = "Opis";

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	public byte[] generateAnswersExcelFile(List<AnswerGet> answersList, QuestionGet question) throws IOException {

		log.info("Starting generating answers Excel file for question = {}", question);
		log.info("answersList.size()={}", answersList.size());

		String[] columns = {ID_COLUMN, TRESC_ODPOWIEDZI_COLUMN, DATA_UTWORZENIA_COLUMN, DATA_MODYFIKACJI_COLUMN, UZYTKOWNIK_COLUMN};
		Workbook workbook = new XSSFWorkbook();

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

			Cell creationDateCell = row.createCell(2);
			creationDateCell.setCellValue(answer.getCreatedAtAsString());
			creationDateCell.setCellStyle(dateCellStyle);

			Cell modificationDateCell = row.createCell(3);
			modificationDateCell.setCellValue(answer.getUpdatedAtAsString());
			modificationDateCell.setCellStyle(dateCellStyle);

			row.createCell(4).setCellValue(answer.getUser());
		}

		for (int i = 0; i < columns.length; i++) {
			answersSheet.autoSizeColumn(i);
		}

		Sheet questionSheet = workbook.createSheet("Pytanie");

		Row row1 = questionSheet.createRow(0);
		Cell cellA1 = row1.createCell(0);
		cellA1.setCellValue(ID_COLUMN);
		cellA1.setCellStyle(headerCellStyle);
		row1.createCell(1).setCellValue(question.getId().toString());

		Row row2 = questionSheet.createRow(1);
		Cell cellA2 = row2.createCell(0);
		cellA2.setCellValue(TYTUL_COLUMN);
		cellA2.setCellStyle(headerCellStyle);
		row2.createCell(1).setCellValue(question.getTitle());

		Row row3 = questionSheet.createRow(2);
		Cell cellA3 = row3.createCell(0);
		cellA3.setCellValue(OPIS_COLUMN);
		cellA3.setCellStyle(headerCellStyle);
		row3.createCell(1).setCellValue(addValueWithNewLines(question.getDescription()));

		Row row4 = questionSheet.createRow(3);
		Cell cellA4 = row4.createCell(0);
		cellA4.setCellValue(DATA_UTWORZENIA_COLUMN);
		cellA4.setCellStyle(headerCellStyle);
		Cell cellB4 = row4.createCell(1);
		cellB4.setCellValue(question.getCreatedAtAsString());
		cellB4.setCellStyle(dateCellStyle);

		Row row5 = questionSheet.createRow(4);
		Cell cellA5 = row5.createCell(0);
		cellA5.setCellValue(DATA_MODYFIKACJI_COLUMN);
		cellA5.setCellStyle(headerCellStyle);
		Cell cellB5 = row5.createCell(1);
		cellB5.setCellValue(question.getUpdatedAtAsString());
		cellB5.setCellStyle(dateCellStyle);

		Row row6 = questionSheet.createRow(5);
		Cell cellA6 = row6.createCell(0);
		cellA6.setCellValue(UZYTKOWNIK_COLUMN);
		cellA6.setCellStyle(headerCellStyle);
		row6.createCell(1).setCellValue(question.getUser());

		questionSheet.autoSizeColumn(0);
		questionSheet.autoSizeColumn(1);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		outputStream.close();
		workbook.close();

		log.info("Answers Excel file generated");
		return outputStream.toByteArray();
	}

	public byte[] generateQuestionsExcelFile(List<QuestionGet> questionsList) throws IOException {

		log.info("Starting generating questions Excel file");
		log.info("questionsList.size()={}", questionsList.size());

		String[] columns = { ID_COLUMN, TYTUL_COLUMN, OPIS_COLUMN, DATA_UTWORZENIA_COLUMN, DATA_MODYFIKACJI_COLUMN, UZYTKOWNIK_COLUMN};
		Workbook workbook = new XSSFWorkbook();
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

			Cell creationDateCell = row.createCell(3);
			creationDateCell.setCellValue(question.getCreatedAtAsString());
			creationDateCell.setCellStyle(dateCellStyle);

			Cell modificationDateCell = row.createCell(4);
			modificationDateCell.setCellValue(question.getUpdatedAtAsString());
			modificationDateCell.setCellStyle(dateCellStyle);

			row.createCell(5).setCellValue(question.getUser());
		}

		for (int i = 0; i < columns.length; i++) {
			sheet.autoSizeColumn(i);
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		outputStream.close();
		workbook.close();

		log.info("Questions Excel file generated");
		return outputStream.toByteArray();
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
		} else {
			return value.replace("\n", " ");
		}
	}
}
