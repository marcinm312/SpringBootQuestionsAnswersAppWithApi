package pl.marcinm312.springdatasecurityex.answer.model.dto;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import pl.marcinm312.springdatasecurityex.answer.model.dto.AnswerGet;

class AnswerGetTest {

	@Test
	void equalsHashCode_differentCases() {
		EqualsVerifier.forClass(AnswerGet.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.withIgnoredFields("createdAt", "updatedAt")
				.verify();
	}
}