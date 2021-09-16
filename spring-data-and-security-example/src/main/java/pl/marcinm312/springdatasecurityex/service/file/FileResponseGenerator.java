package pl.marcinm312.springdatasecurityex.service.file;

import com.itextpdf.text.DocumentException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import pl.marcinm312.springdatasecurityex.enums.FileTypes;
import pl.marcinm312.springdatasecurityex.model.question.dto.QuestionGet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileResponseGenerator {

	private FileResponseGenerator() {

	}

	public static ResponseEntity<Object> generateResponseWithFile(File file) throws IOException {
		ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));
		return ResponseEntity.ok().contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
				.body(resource);
	}

	public static ResponseEntity<Object> generateQuestionsFile(List<QuestionGet> questionsList, FileTypes filetype)
			throws IOException, DocumentException {
		File file;
		if (filetype.equals(FileTypes.EXCEL)) {
			file = ExcelGenerator.generateQuestionsExcelFile(questionsList);
		} else {
			file = PdfGenerator.generateQuestionsPdfFile(questionsList);
		}
		return FileResponseGenerator.generateResponseWithFile(file);
	}
}
