package pl.marcinm312.springdatasecurityex.service.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import pl.marcinm312.springdatasecurityex.model.Answer;
import pl.marcinm312.springdatasecurityex.model.Question;

@Service
public class ExcelGenerator {

	protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	public File generateAnswersExcelFile(List<Answer> answersList, Question question) throws IOException {
		log.info("Starting generating answers Excel file for question = " + question.toString());
		log.info("answersList.size()=" + answersList.size());

		String filePath = "files\\Odpowiedzi.xlsx";

		String[] columns = { "Id", "Treść odpowiedzi", "Data utworzenia", "Data modyfikacji", "Użytkownik" };
		Workbook workbook = new XSSFWorkbook();

		CellStyle headerCellStyle = getHeaderCellStyle(workbook, true, 14, IndexedColors.RED.getIndex());
		CellStyle dateCellStyle = getDateCellStyle(workbook);

		Sheet answersSheet = workbook.createSheet("Odpowiedzi");

		Row headerRow = answersSheet.createRow(0);

		for (int i = 0; i < columns.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(headerCellStyle);
		}

		int rowNum = 1;
		for (Answer answer : answersList) {
			Row row = answersSheet.createRow(rowNum++);

			row.createCell(0).setCellValue(answer.getId());
			row.createCell(1).setCellValue(answer.getText());

			Cell creationDateCell = row.createCell(2);
			creationDateCell.setCellValue(answer.getCreatedAtAsString());
			creationDateCell.setCellStyle(dateCellStyle);

			Cell modificationDateCell = row.createCell(3);
			modificationDateCell.setCellValue(answer.getUpdatedAtAsString());
			modificationDateCell.setCellStyle(dateCellStyle);

			row.createCell(4).setCellValue(answer.getUser().getUsername());
		}

		for (int i = 0; i < columns.length; i++) {
			answersSheet.autoSizeColumn(i);
		}

		Sheet questionSheet = workbook.createSheet("Pytanie");

		Row row1 = questionSheet.createRow(0);
		Cell cellA1 = row1.createCell(0);
		cellA1.setCellValue("Id");
		cellA1.setCellStyle(headerCellStyle);
		row1.createCell(1).setCellValue(question.getId().toString());

		Row row2 = questionSheet.createRow(1);
		Cell cellA2 = row2.createCell(0);
		cellA2.setCellValue("Tytuł");
		cellA2.setCellStyle(headerCellStyle);
		row2.createCell(1).setCellValue(question.getTitle());

		Row row3 = questionSheet.createRow(2);
		Cell cellA3 = row3.createCell(0);
		cellA3.setCellValue("Opis");
		cellA3.setCellStyle(headerCellStyle);
		row3.createCell(1).setCellValue(question.getDescription());

		Row row4 = questionSheet.createRow(3);
		Cell cellA4 = row4.createCell(0);
		cellA4.setCellValue("Data utworzenia");
		cellA4.setCellStyle(headerCellStyle);
		Cell cellB4 = row4.createCell(1);
		cellB4.setCellValue(question.getCreatedAtAsString());
		cellB4.setCellStyle(dateCellStyle);

		Row row5 = questionSheet.createRow(4);
		Cell cellA5 = row5.createCell(0);
		cellA5.setCellValue("Data modyfikacji");
		cellA5.setCellStyle(headerCellStyle);
		Cell cellB5 = row5.createCell(1);
		cellB5.setCellValue(question.getUpdatedAtAsString());
		cellB5.setCellStyle(dateCellStyle);

		Row row6 = questionSheet.createRow(5);
		Cell cellA6 = row6.createCell(0);
		cellA6.setCellValue("Użytkownik");
		cellA6.setCellStyle(headerCellStyle);
		row6.createCell(1).setCellValue(question.getUser().getUsername());

		questionSheet.autoSizeColumn(0);
		questionSheet.autoSizeColumn(1);

		FileOutputStream fileOut = new FileOutputStream(filePath);
		workbook.write(fileOut);
		fileOut.close();

		workbook.close();

		File file = new File(filePath);
		log.info("Answers Excel file generated");
		return file;
	}

	public File generateQuestionsExcelFile(List<Question> questionsList) throws IOException {
		log.info("Starting generating questions Excel file");
		log.info("questionsList.size()=" + questionsList.size());
		String filePath = "files\\Pytania.xlsx";

		String[] columns = { "Id", "Tytuł", "Opis", "Data utworzenia", "Data modyfikacji", "Użytkownik" };
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Pytania");
		CellStyle headerCellStyle = getHeaderCellStyle(workbook, true, 14, IndexedColors.RED.getIndex());

		Row headerRow = sheet.createRow(0);

		for (int i = 0; i < columns.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(headerCellStyle);
		}

		CellStyle dateCellStyle = getDateCellStyle(workbook);

		int rowNum = 1;
		for (Question question : questionsList) {
			Row row = sheet.createRow(rowNum++);

			row.createCell(0).setCellValue(question.getId());
			row.createCell(1).setCellValue(question.getTitle());
			row.createCell(2).setCellValue(question.getDescription());

			Cell creationDateCell = row.createCell(3);
			creationDateCell.setCellValue(question.getCreatedAtAsString());
			creationDateCell.setCellStyle(dateCellStyle);

			Cell modificationDateCell = row.createCell(4);
			modificationDateCell.setCellValue(question.getUpdatedAtAsString());
			modificationDateCell.setCellStyle(dateCellStyle);

			row.createCell(5).setCellValue(question.getUser().getUsername());
		}

		for (int i = 0; i < columns.length; i++) {
			sheet.autoSizeColumn(i);
		}

		FileOutputStream fileOut = new FileOutputStream(filePath);
		workbook.write(fileOut);
		fileOut.close();

		workbook.close();

		File file = new File(filePath);
		log.info("Questions Excel file generated");
		return file;
	}

	private CellStyle getDateCellStyle(Workbook workbook) {
		CreationHelper createHelper = workbook.getCreationHelper();
		CellStyle dateCellStyle = workbook.createCellStyle();
		dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd hh:mm:ss"));
		return dateCellStyle;
	}

	private CellStyle getHeaderCellStyle(Workbook workbook, boolean bold, int fontSize, short color) {
		Font headerFont = workbook.createFont();
		headerFont.setBold(bold);
		headerFont.setFontHeightInPoints((short) fontSize);
		headerFont.setColor(color);
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		return headerCellStyle;
	}
}
