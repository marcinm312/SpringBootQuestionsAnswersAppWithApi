package pl.marcinm312.springdatasecurityex.shared.file;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@UtilityClass
public class FileResponseGenerator {

	public static ResponseEntity<Object> generateResponseWithFile(byte[] bytes, String fileName) {
		ByteArrayResource resource = new ByteArrayResource(bytes);
		return ResponseEntity.ok().contentLength(bytes.length)
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(resource);
	}
}
