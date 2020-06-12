package pl.marcinm312.springdatasecurityex.service.file;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import pl.marcinm312.springdatasecurityex.model.Answer;
import pl.marcinm312.springdatasecurityex.model.Question;

@Service
public class PdfGenerator {

	protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	public File generateQuestionsPdfFile(List<Question> questionsList) throws DocumentException, IOException {
		log.info("Starting generating questions PDF file");
		log.info("questionsList.size()=" + questionsList.size());
		BaseFont helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
		Font helvetica18 = new Font(helvetica, 18);
		Font helvetica12 = new Font(helvetica, 12);
		String filePath = "files\\Pytania.pdf";
		Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
		PdfWriter.getInstance(document, new FileOutputStream(filePath));
		document.open();
		Paragraph title = new Paragraph("Lista pytań", helvetica18);
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);
		document.add(Chunk.NEWLINE);
		PdfPTable table = new PdfPTable(6);
		createAndAddCellToTable("Id", Color.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Tytuł", Color.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Opis", Color.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Data utworzenia", Color.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Data modyfikacji", Color.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Użytkownik", Color.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		for (Question question : questionsList) {
			createAndAddCellToTable(question.getId().toString(), Color.WHITE, Element.ALIGN_LEFT, helvetica12, table);
			createAndAddCellToTable(question.getTitle(), Color.WHITE, Element.ALIGN_LEFT, helvetica12, table);
			createAndAddCellToTable(question.getDescription(), Color.WHITE, Element.ALIGN_LEFT, helvetica12, table);
			createAndAddCellToTable(question.getCreatedAtAsString(), Color.WHITE, Element.ALIGN_LEFT, helvetica12,
					table);
			createAndAddCellToTable(question.getUpdatedAtAsString(), Color.WHITE, Element.ALIGN_LEFT, helvetica12,
					table);
			createAndAddCellToTable(question.getUser().getUsername(), Color.WHITE, Element.ALIGN_LEFT, helvetica12,
					table);
		}
		int[] szerokosci = { 40, 150, 150, 120, 120, 120 };
		table.setWidths(szerokosci);
		table.setTotalWidth(700);
		table.setLockedWidth(true);
		document.add(table);
		document.close();
		File file = new File(filePath);
		log.info("Questions PDF file generated");
		return file;
	}

	public File generateAnswersPdfFile(List<Answer> answersList, Question question)
			throws DocumentException, IOException {
		log.info("Starting generating answers PDF file for question = " + question.toString());
		log.info("answersList.size()=" + answersList.size());
		BaseFont helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
		Font helvetica18 = new Font(helvetica, 18);
		Font helvetica12 = new Font(helvetica, 12);
		String filePath = "files\\Odpowiedzi.pdf";
		Document document = new Document(PageSize.A4.rotate(), 70, 70, 20, 20);
		PdfWriter.getInstance(document, new FileOutputStream(filePath));
		document.open();
		Paragraph title = new Paragraph("Lista odpowiedzi", helvetica18);
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);
		document.add(Chunk.NEWLINE);
		Paragraph questionTitle = new Paragraph("Tytuł pytania: " + question.getTitle(), helvetica12);
		document.add(questionTitle);
		Paragraph questionDescription = new Paragraph("Opis: " + question.getDescription(), helvetica12);
		document.add(questionDescription);
		Paragraph questionUser = new Paragraph("Użytkownik: " + question.getUser().getUsername(), helvetica12);
		document.add(questionUser);
		document.add(Chunk.NEWLINE);
		PdfPTable table = new PdfPTable(5);
		createAndAddCellToTable("Id", Color.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Treść odpowiedzi", Color.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Data utworzenia", Color.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Data modyfikacji", Color.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		createAndAddCellToTable("Użytkownik", Color.GRAY, Element.ALIGN_CENTER, helvetica12, table);
		for (Answer answer : answersList) {
			createAndAddCellToTable(answer.getId().toString(), Color.WHITE, Element.ALIGN_LEFT, helvetica12, table);
			createAndAddCellToTable(answer.getText(), Color.WHITE, Element.ALIGN_LEFT, helvetica12, table);
			createAndAddCellToTable(answer.getCreatedAtAsString(), Color.WHITE, Element.ALIGN_LEFT, helvetica12, table);
			createAndAddCellToTable(answer.getUpdatedAtAsString(), Color.WHITE, Element.ALIGN_LEFT, helvetica12, table);
			createAndAddCellToTable(answer.getUser().getUsername(), Color.WHITE, Element.ALIGN_LEFT, helvetica12,
					table);
		}
		int[] szerokosci = { 40, 300, 120, 120, 120 };
		table.setWidths(szerokosci);
		table.setTotalWidth(700);
		table.setLockedWidth(true);
		document.add(table);
		document.close();
		File file = new File(filePath);
		log.info("Answers PDF file generated");
		return file;
	}

	private void createAndAddCellToTable(String text, Color color, int alignment, Font font, PdfPTable table) {
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
