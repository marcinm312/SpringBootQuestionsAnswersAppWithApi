package pl.marcinm312.springdatasecurityex.shared.file;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
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

	private final JasperReport questionsCompiledReport;
	private final JasperReport answersCompiledReport;

	public PdfGenerator() throws JRException {

		JRPropertiesUtil jrPropertiesUtil = JRPropertiesUtil.getInstance(DefaultJasperReportsContext.getInstance());
		jrPropertiesUtil.setProperty("net.sf.jasperreports.default.pdf.encoding", "Cp1250");
		jrPropertiesUtil.setProperty("net.sf.jasperreports.compiler.xml.parser.cache.schemas", "false");

		InputStream questionsReportTemplate = getClass().getResourceAsStream("/jasper/QuestionsReport.jrxml");
		questionsCompiledReport = JasperCompileManager.compileReport(questionsReportTemplate);

		InputStream answersReportTemplate = getClass().getResourceAsStream("/jasper/AnswersReport.jrxml");
		answersCompiledReport = JasperCompileManager.compileReport(answersReportTemplate);
	}

	public byte[] generateQuestionsPdfFile(List<QuestionGet> questionsList) throws FileException {

		log.info("Starting generating questions PDF file");
		log.info("questionsList.size()={}", questionsList.size());

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(questionsList);

			Map<String, Object> parameters = new HashMap<>();
			parameters.put("questionsDataSource", dataSource);

			printJasperPdf(questionsCompiledReport, parameters, outputStream);

			log.info("Questions PDF file generated");
			return outputStream.toByteArray();

		} catch (Exception e) {
			log.error("Error while generating questions PDF file: {}", e.getMessage());
			throw new FileException(e.getMessage());
		}
	}

	public byte[] generateAnswersPdfFile(List<AnswerGet> answersList, QuestionGet question) throws FileException {

		log.info("Starting generating answers PDF file for question = {}", question);
		log.info("answersList.size()={}", answersList.size());

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(answersList);

			Map<String, Object> parameters = new HashMap<>();
			parameters.put("questionTitle", question.getTitle());
			parameters.put("questionDescription", question.getDescription());
			parameters.put("userName", question.getUser());
			parameters.put("answersDataSource", dataSource);

			printJasperPdf(answersCompiledReport, parameters, outputStream);

			log.info("Answers PDF file generated");
			return outputStream.toByteArray();

		} catch (Exception e) {
			log.error("Error while generating answers PDF file: {}", e.getMessage());
			throw new FileException(e.getMessage());
		}
	}

	private void printJasperPdf(JasperReport compiledReport, Map<String, Object> parameters, ByteArrayOutputStream outputStream)
			throws JRException {

		JasperPrint jasperPrint = JasperFillManager.fillReport(compiledReport, parameters, new JREmptyDataSource());
		JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
	}
}
