package pl.marcinm312.springquestionsanswers.shared.file;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileResponseGenerator {

	public static ResponseEntity<ByteArrayResource> generateResponseWithFile(byte[] bytes, String fileName,
																			 String mimeType) {

		return ResponseEntity.ok().contentLength(bytes.length)
				.contentType(MediaType.parseMediaType(mimeType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(new ByteArrayResource(bytes));
	}
}
