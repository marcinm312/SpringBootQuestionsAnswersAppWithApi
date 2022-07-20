package pl.marcinm312.springdatasecurityex.shared.file;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.shared.exception.FileException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static pl.marcinm312.springdatasecurityex.shared.file.Columns.*;

@Slf4j
public class PdfGenerator {

	private static final String OF_QUESTION = " pytania: ";
	private final Font helvetica18;
	private final Font helvetica12;


	public PdfGenerator() throws DocumentException, IOException {
		BaseFont helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
		helvetica18 = new Font(helvetica, 18);
		helvetica12 = new Font(helvetica, 12);
	}

	public byte[] generateQuestionsPdfFile(List<QuestionGet> questionsList) {

		log.info("Starting generating questions PDF file");
		log.info("questionsList.size()={}", questionsList.size());

		Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			PdfWriter.getInstance(document, outputStream);
			document.open();

			Paragraph title = new Paragraph("Lista pytań", helvetica18);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);
			document.add(Chunk.NEWLINE);
			PdfPTable table = new PdfPTable(6);
			createAndAddCellToTable(ID_COLUMN, BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
			createAndAddCellToTable(QUESTION_TITLE_COLUMN, BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
			createAndAddCellToTable(QUESTION_DESCRIPTION_COLUMN, BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
			addCommonsColumnsHeaders(table);
			for (QuestionGet question : questionsList) {
				createAndAddCellToTable(question.getId().toString(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12, table);
				createAndAddCellToTable(question.getTitle(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12, table);
				createAndAddCellToTable(question.getDescription(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12, table);
				createAndAddCellToTable(question.getCreatedAtAsString(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12,
						table);
				createAndAddCellToTable(question.getUpdatedAtAsString(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12,
						table);
				createAndAddCellToTable(question.getUser(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12,
						table);
			}
			int[] widths = {40, 150, 150, 120, 120, 120};
			table.setWidths(widths);
			table.setTotalWidth(700);
			table.setLockedWidth(true);
			document.add(table);

			document.close();

			log.info("Questions PDF file generated");
			return outputStream.toByteArray();

		} catch (Exception e) {
			document.close();
			throw new FileException(e.getMessage());
		}
	}

	public byte[] generateAnswersPdfFile(List<AnswerGet> answersList, QuestionGet question) {

		log.info("Starting generating answers PDF file for question = {}", question);
		log.info("answersList.size()={}", answersList.size());

		Document document = new Document(PageSize.A4.rotate(), 70, 70, 20, 20);
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			PdfWriter.getInstance(document, outputStream);
			document.open();

			Paragraph title = new Paragraph("Lista odpowiedzi", helvetica18);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);
			document.add(Chunk.NEWLINE);
			Paragraph questionTitle = new Paragraph(QUESTION_TITLE_COLUMN + OF_QUESTION + question.getTitle(), helvetica12);
			document.add(questionTitle);
			Paragraph questionDescription = new Paragraph(QUESTION_DESCRIPTION_COLUMN + OF_QUESTION + question.getDescription(), helvetica12);
			document.add(questionDescription);
			Paragraph questionUser = new Paragraph(USER_COLUMN + ": " + question.getUser(), helvetica12);
			document.add(questionUser);
			document.add(Chunk.NEWLINE);
			PdfPTable table = new PdfPTable(5);
			createAndAddCellToTable(ID_COLUMN, BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
			createAndAddCellToTable(ANSWER_TEXT_COLUMN, BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
			addCommonsColumnsHeaders(table);
			for (AnswerGet answer : answersList) {
				createAndAddCellToTable(answer.getId().toString(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12, table);
				createAndAddCellToTable(answer.getText(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12, table);
				createAndAddCellToTable(answer.getCreatedAtAsString(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12, table);
				createAndAddCellToTable(answer.getUpdatedAtAsString(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12, table);
				createAndAddCellToTable(answer.getUser(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12,
						table);
			}
			int[] widths = {40, 300, 120, 120, 120};
			table.setWidths(widths);
			table.setTotalWidth(700);
			table.setLockedWidth(true);
			document.add(table);

			document.close();

			log.info("Answers PDF file generated");
			return outputStream.toByteArray();

		} catch (Exception e) {
			document.close();
			throw new FileException(e.getMessage());
		}
	}

	private void createAndAddCellToTable(String text, BaseColor color, int alignment, Font font, PdfPTable table) {
		PdfPCell cell = new PdfPCell(new Paragraph(text, font));
		cell.setBackgroundColor(color);
		cell.setHorizontalAlignment(alignment);
		cell.setPaddingBottom(4);
		cell.setPaddingLeft(4);
		cell.setPaddingRight(4);
		cell.setPaddingTop(4);
		table.addCell(cell);
	}

	private void addCommonsColumnsHeaders(PdfPTable table) {
		createAndAddCellToTable(CREATION_DATE_COLUMN, BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable(MODIFICATION_DATE_COLUMN, BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable(USER_COLUMN, BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
	}
}
