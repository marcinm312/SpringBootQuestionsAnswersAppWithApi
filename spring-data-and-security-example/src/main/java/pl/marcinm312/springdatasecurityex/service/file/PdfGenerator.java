package pl.marcinm312.springdatasecurityex.service.file;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.LoggerFactory;
import pl.marcinm312.springdatasecurityex.model.answer.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfGenerator {

	private PdfGenerator() {

	}

	public static File generateQuestionsPdfFile(List<QuestionGet> questionsList) throws DocumentException, IOException {
		org.slf4j.Logger log = LoggerFactory.getLogger(PdfGenerator.class);

		log.info("Starting generating questions PDF file");
		log.info("questionsList.size()={}", questionsList.size());

		String fileId = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
		String filePath = "files" + FileSystems.getDefault().getSeparator() + "Pytania_" + fileId + ".pdf";

		Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
		PdfWriter.getInstance(document, new FileOutputStream(filePath));
		document.open();
		Paragraph title = new Paragraph("Lista pytań", getHelvetica18Font());
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);
		document.add(Chunk.NEWLINE);
		PdfPTable table = new PdfPTable(6);
		createAndAddCellToTable("Id", BaseColor.GRAY, Element.ALIGN_CENTER, getHelvetica12Font(), table);
		createAndAddCellToTable("Tytuł", BaseColor.GRAY, Element.ALIGN_CENTER, getHelvetica12Font(), table);
		createAndAddCellToTable("Opis", BaseColor.GRAY, Element.ALIGN_CENTER, getHelvetica12Font(), table);
		createAndAddCellToTable("Data utworzenia", BaseColor.GRAY, Element.ALIGN_CENTER, getHelvetica12Font(), table);
		createAndAddCellToTable("Data modyfikacji", BaseColor.GRAY, Element.ALIGN_CENTER, getHelvetica12Font(), table);
		createAndAddCellToTable("Użytkownik", BaseColor.GRAY, Element.ALIGN_CENTER, getHelvetica12Font(), table);
		for (QuestionGet question : questionsList) {
			createAndAddCellToTable(question.getId().toString(), BaseColor.WHITE, Element.ALIGN_LEFT, getHelvetica12Font(), table);
			createAndAddCellToTable(question.getTitle(), BaseColor.WHITE, Element.ALIGN_LEFT, getHelvetica12Font(), table);
			createAndAddCellToTable(question.getDescription(), BaseColor.WHITE, Element.ALIGN_LEFT, getHelvetica12Font(), table);
			createAndAddCellToTable(question.getCreatedAtAsString(), BaseColor.WHITE, Element.ALIGN_LEFT, getHelvetica12Font(),
					table);
			createAndAddCellToTable(question.getUpdatedAtAsString(), BaseColor.WHITE, Element.ALIGN_LEFT, getHelvetica12Font(),
					table);
			createAndAddCellToTable(question.getUser(), BaseColor.WHITE, Element.ALIGN_LEFT, getHelvetica12Font(),
					table);
		}
		int[] szerokosci = {40, 150, 150, 120, 120, 120};
		table.setWidths(szerokosci);
		table.setTotalWidth(700);
		table.setLockedWidth(true);
		document.add(table);
		document.close();
		File file = new File(filePath);
		log.info("Questions PDF file generated");
		return file;
	}

	public static File generateAnswersPdfFile(List<AnswerGet> answersList, QuestionGet question)
			throws DocumentException, IOException {
		org.slf4j.Logger log = LoggerFactory.getLogger(PdfGenerator.class);

		log.info("Starting generating answers PDF file for question = {}", question);
		log.info("answersList.size()={}", answersList.size());

		String fileId = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
		String filePath = "files" + FileSystems.getDefault().getSeparator() + "Odpowiedzi_" + fileId + ".pdf";

		Document document = new Document(PageSize.A4.rotate(), 70, 70, 20, 20);
		PdfWriter.getInstance(document, new FileOutputStream(filePath));
		document.open();
		Paragraph title = new Paragraph("Lista odpowiedzi", getHelvetica18Font());
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);
		document.add(Chunk.NEWLINE);
		Paragraph questionTitle = new Paragraph("Tytuł pytania: " + question.getTitle(), getHelvetica12Font());
		document.add(questionTitle);
		Paragraph questionDescription = new Paragraph("Opis: " + question.getDescription(), getHelvetica12Font());
		document.add(questionDescription);
		Paragraph questionUser = new Paragraph("Użytkownik: " + question.getUser(), getHelvetica12Font());
		document.add(questionUser);
		document.add(Chunk.NEWLINE);
		PdfPTable table = new PdfPTable(5);
		createAndAddCellToTable("Id", BaseColor.GRAY, Element.ALIGN_CENTER, getHelvetica12Font(), table);
		createAndAddCellToTable("Treść odpowiedzi", BaseColor.GRAY, Element.ALIGN_CENTER, getHelvetica12Font(), table);
		createAndAddCellToTable("Data utworzenia", BaseColor.GRAY, Element.ALIGN_CENTER, getHelvetica12Font(), table);
		createAndAddCellToTable("Data modyfikacji", BaseColor.GRAY, Element.ALIGN_CENTER, getHelvetica12Font(), table);
		createAndAddCellToTable("Użytkownik", BaseColor.GRAY, Element.ALIGN_CENTER, getHelvetica12Font(), table);
		for (AnswerGet answer : answersList) {
			createAndAddCellToTable(answer.getId().toString(), BaseColor.WHITE, Element.ALIGN_LEFT, getHelvetica12Font(), table);
			createAndAddCellToTable(answer.getText(), BaseColor.WHITE, Element.ALIGN_LEFT, getHelvetica12Font(), table);
			createAndAddCellToTable(answer.getCreatedAtAsString(), BaseColor.WHITE, Element.ALIGN_LEFT, getHelvetica12Font(), table);
			createAndAddCellToTable(answer.getUpdatedAtAsString(), BaseColor.WHITE, Element.ALIGN_LEFT, getHelvetica12Font(), table);
			createAndAddCellToTable(answer.getUser(), BaseColor.WHITE, Element.ALIGN_LEFT, getHelvetica12Font(),
					table);
		}
		int[] szerokosci = {40, 300, 120, 120, 120};
		table.setWidths(szerokosci);
		table.setTotalWidth(700);
		table.setLockedWidth(true);
		document.add(table);
		document.close();
		File file = new File(filePath);
		log.info("Answers PDF file generated");
		return file;
	}

	private static void createAndAddCellToTable(String text, BaseColor color, int alignment, Font font, PdfPTable table) {
		PdfPCell cell = new PdfPCell(new Paragraph(text, font));
		cell.setBackgroundColor(color);
		cell.setHorizontalAlignment(alignment);
		cell.setPaddingBottom(4);
		cell.setPaddingLeft(4);
		cell.setPaddingRight(4);
		cell.setPaddingTop(4);
		table.addCell(cell);
	}

	private static BaseFont getHelveticaBaseFont() throws DocumentException, IOException {
		return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
	}

	private static Font getHelvetica18Font() throws DocumentException, IOException {
		return new Font(getHelveticaBaseFont(), 18);
	}

	private static Font getHelvetica12Font() throws DocumentException, IOException {
		return new Font(getHelveticaBaseFont(), 12);
	}
}
