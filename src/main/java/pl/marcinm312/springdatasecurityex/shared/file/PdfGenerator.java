package pl.marcinm312.springdatasecurityex.shared.file;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;
import pl.marcinm312.springdatasecurityex.question.model.dto.QuestionGet;
import pl.marcinm312.springdatasecurityex.shared.exception.FileException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Component
public class PdfGenerator {

	private static final String OF_QUESTION = " pytania: ";


	public PdfGenerator() {

	}

	public byte[] generateQuestionsPdfFile(List<QuestionGet> questionsList) throws FileException {

		log.info("Starting generating questions PDF file");
		log.info("questionsList.size()={}", questionsList.size());

		try (PDDocument document = new PDDocument();
			 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
			document.addPage(page);

			PDPageContentStream contentStream = new PDPageContentStream(document, page);

			contentStream.beginText();
			contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22);
			contentStream.newLineAtOffset(50, 500);
			contentStream.showText("Document title");
			contentStream.endText();
			contentStream.close();

			List<List> data = new ArrayList();
			data.add(new ArrayList<>(
					Arrays.asList("Column One", "Column Two", "Column Three", "Column Four", "Column Five")));
			for (int i = 1; i <= 100; i++) {
				data.add(new ArrayList<>(
						Arrays.asList(
								"Row " + i + " Col One",
								"Row " + i + " Col Two yyyyyyyyyyyyy yyyyyy yyyyyyyyyyyyyy yyyyyyyyyyyy yyyyyyyyyyyyyyy yyyyyyyyyyyy yyyyyy",
								"Row " + i + " Col Three yyyyyyyyyyyyy yyyyyy yyyyyyyyyyyyyy yyyyyyyyyyyy yyyyyyyyyyyyyyy yyyyyyyyyyyy yyyyyy yyyyyyyyyyyyy yyyyyy yyyyyyyyyyyyyy yyyyyyyyyyyy yyyyyyyyyyyyyyy yyyyyyyyyyyy yyyyyy",
								"Row " + i + " Col Four",
								"Row " + i + " Col Five"
						)));
			}

			//Dummy Table
			float margin = 50;
// starting y position is whole page height subtracted by top and bottom margin
			float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
// we want table across whole page width (subtracted by left and right margin ofcourse)
			float tableWidth = page.getMediaBox().getWidth() - (2 * margin);

			float yStart = yStartNewPage;
			float bottomMargin = 70;

			BaseTable dataTable = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, document, page, true, true);
			DataTable t = new DataTable(dataTable, page);
			t.addListToTable(data, DataTable.HASHEADER);
			dataTable.draw();

			//contentStream.close();
			//document.addPage(page);

			document.save(outputStream);

			//int[] widths = {40, 150, 150, 120, 120, 120};

			log.info("Questions PDF file generated");
			return outputStream.toByteArray();

		} catch (Exception e) {
			throw new FileException(e.getMessage());
		}
	}

	public byte[] generateAnswersPdfFile(List<AnswerGet> answersList, QuestionGet question) throws FileException {

		log.info("Starting generating answers PDF file for question = {}", question);
		log.info("answersList.size()={}", answersList.size());

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			JRPropertiesUtil jrPropertiesUtil = JRPropertiesUtil.getInstance(DefaultJasperReportsContext.getInstance());
			jrPropertiesUtil.setProperty("net.sf.jasperreports.default.pdf.encoding", "Cp1250");
			jrPropertiesUtil.setProperty("net.sf.jasperreports.compiler.xml.parser.cache.schemas", "false");

			InputStream jasperReportTemplate = getClass().getResourceAsStream("/AnswersReport.jrxml");

			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(answersList);

			Map<String, Object> parameters = new HashMap<>();
			parameters.put("questionTitle", question.getTitle());
			parameters.put("questionDescription", question.getDescription());
			parameters.put("userName", question.getUser());
			parameters.put("answersDataSource", dataSource);

			JasperReport jasperReport = JasperCompileManager.compileReport(jasperReportTemplate);
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
			JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

			log.info("Answers PDF file generated");
			return outputStream.toByteArray();

		} catch (Exception e) {
			throw new FileException(e.getMessage());
		}
	}
}
