package pl.marcinm312.springquestionsanswers.config.stringtrimmer.api;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.module.SimpleModule;

import java.util.List;

@Component
public class ApiStringTrimModule extends SimpleModule {

	public ApiStringTrimModule() {

		addDeserializer(String.class, new StdScalarDeserializer<>(String.class) {

			@Override
			public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws JacksonException {

				List<String> fieldsToNotTrim = List.of("currentPassword", "password", "confirmPassword");
				String fieldName = jsonParser.currentName();
				if (fieldsToNotTrim.contains(fieldName)) {
					return jsonParser.getValueAsString();
				}
				String value = jsonParser.getValueAsString();
				return value == null ? null : value.trim();
			}

		});
	}
}
