package pl.marcinm312.springdatasecurityex.service.file;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileResponseGenerator {

	public static ResponseEntity<?> generateResponseWithFile(File file) throws IOException {
		ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));
		return ResponseEntity.ok().contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
				.body(resource);
	}
}
