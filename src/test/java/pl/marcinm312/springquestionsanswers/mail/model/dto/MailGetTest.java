package pl.marcinm312.springquestionsanswers.mail.model.dto;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class MailGetTest {

	@Test
	void equalsHashCode_differentCases() {
		EqualsVerifier.forClass(MailGet.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.withIgnoredFields("createdAt", "updatedAt", "dateFormat")
				.verify();
	}
}