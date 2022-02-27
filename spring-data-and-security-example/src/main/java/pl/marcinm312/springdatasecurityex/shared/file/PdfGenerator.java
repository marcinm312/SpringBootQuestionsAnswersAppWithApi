package pl.marcinm312.springdatasecurityex.shared.file;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class PdfGenerator {

	private final Font helvetica18;
	private final Font helvetica12;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	public PdfGenerator() throws DocumentException, IOException {
		BaseFont helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
		helvetica18 = new Font(helvetica, 18);
		helvetica12 = new Font(helvetica, 12);
	}

	public byte[] generateQuestionsPdfFile(List<QuestionGet> questionsList) throws DocumentException, IOException {

		log.info("Starting generating questions PDF file");
		log.info("questionsList.size()={}", questionsList.size());

		Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, outputStream);
		document.open();

		Paragraph title = new Paragraph("Lista pytań", helvetica18);
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);
		document.add(Chunk.NEWLINE);
		PdfPTable table = new PdfPTable(6);
		createAndAddCellToTable("Id", BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Tytuł", BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Opis", BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Data utworzenia", BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Data modyfikacji", BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Użytkownik", BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
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
		int[] szerokosci = {40, 150, 150, 120, 120, 120};
		table.setWidths(szerokosci);
		table.setTotalWidth(700);
		table.setLockedWidth(true);
		document.add(table);

		document.close();
		outputStream.close();

		log.info("Questions PDF file generated");
		return outputStream.toByteArray();
	}

	public byte[] generateAnswersPdfFile(List<AnswerGet> answersList, QuestionGet question)
			throws DocumentException, IOException {

		log.info("Starting generating answers PDF file for question = {}", question);
		log.info("answersList.size()={}", answersList.size());

		Document document = new Document(PageSize.A4.rotate(), 70, 70, 20, 20);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, outputStream);
		document.open();

		Paragraph title = new Paragraph("Lista odpowiedzi", helvetica18);
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);
		document.add(Chunk.NEWLINE);
		Paragraph questionTitle = new Paragraph("Tytuł pytania: " + question.getTitle(), helvetica12);
		document.add(questionTitle);
		Paragraph questionDescription = new Paragraph("Opis: " + question.getDescription(), helvetica12);
		document.add(questionDescription);
		Paragraph questionUser = new Paragraph("Użytkownik: " + question.getUser(), helvetica12);
		document.add(questionUser);
		document.add(Chunk.NEWLINE);
		PdfPTable table = new PdfPTable(5);
		createAndAddCellToTable("Id", BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Treść odpowiedzi", BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Data utworzenia", BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Data modyfikacji", BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Użytkownik", BaseColor.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		for (AnswerGet answer : answersList) {
			createAndAddCellToTable(answer.getId().toString(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12, table);
			createAndAddCellToTable(answer.getText(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12, table);
			createAndAddCellToTable(answer.getCreatedAtAsString(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12, table);
			createAndAddCellToTable(answer.getUpdatedAtAsString(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12, table);
			createAndAddCellToTable(answer.getUser(), BaseColor.WHITE, Element.ALIGN_LEFT, helvetica12,
					table);
		}
		int[] szerokosci = {40, 300, 120, 120, 120};
		table.setWidths(szerokosci);
		table.setTotalWidth(700);
		table.setLockedWidth(true);
		document.add(table);

		document.close();
		outputStream.close();

		log.info("Answers PDF file generated");
		return outputStream.toByteArray();
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
}
