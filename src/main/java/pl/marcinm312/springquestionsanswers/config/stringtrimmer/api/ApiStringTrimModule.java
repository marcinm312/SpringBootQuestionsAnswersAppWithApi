package pl.marcinm312.springquestionsanswers.config.stringtrimmer.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ApiStringTrimModule extends SimpleModule {

	public ApiStringTrimModule() {

		addDeserializer(String.class, new StdScalarDeserializer<>(String.class) {

			@Override
			public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {

				List<String> fieldsToNotTrim = List.of("currentPassword", "password", "confirmPassword");
				String fieldName = jsonParser.currentName();
				if (fieldsToNotTrim.contains(fieldName)) {
					return jsonParser.getValueAsString();
				}
				return jsonParser.getValueAsString().trim();
			}

		});
	}
}
