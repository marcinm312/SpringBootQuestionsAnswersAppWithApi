package pl.marcinm312.springquestionsanswers.config.stringtrimmer.web;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.util.List;

@ControllerAdvice
public class WebStringTrimModule {

	@InitBinder
	public void setupDefaultInitBinder(WebDataBinder binder) {

		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
		PropertyEditorSupport dummyEditor = new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				super.setValue(text);
			}
		};

		List<String> fieldsToNotTrim = List.of("currentPassword", "password", "confirmPassword");
		for (String fieldName : fieldsToNotTrim) {
			binder.registerCustomEditor(String.class, fieldName, dummyEditor);
		}
	}
}
