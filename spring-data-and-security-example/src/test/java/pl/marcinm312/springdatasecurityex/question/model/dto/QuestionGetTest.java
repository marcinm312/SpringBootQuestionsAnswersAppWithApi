package pl.marcinm312.springdatasecurityex.question.model.dto;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class QuestionGetTest {

	@Test
	void equalsHashCode_differentCases() {
		EqualsVerifier.forClass(QuestionGet.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.withIgnoredFields("createdAt", "updatedAt")
				.verify();
	}
}