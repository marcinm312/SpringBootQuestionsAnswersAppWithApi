package pl.marcinm312.springdatasecurityex.model.user.dto;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class UserGetTest {

	@Test
	void equalsHashCode_differentCases() {
		EqualsVerifier.forClass(UserGet.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.withIgnoredFields("createdAt", "updatedAt")
				.verify();
	}
}