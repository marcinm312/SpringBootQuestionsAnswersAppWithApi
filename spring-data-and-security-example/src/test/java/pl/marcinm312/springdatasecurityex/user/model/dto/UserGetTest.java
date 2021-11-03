package pl.marcinm312.springdatasecurityex.user.model.dto;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import pl.marcinm312.springdatasecurityex.user.model.dto.UserGet;

class UserGetTest {

	@Test
	void equalsHashCode_differentCases() {
		EqualsVerifier.forClass(UserGet.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.withIgnoredFields("createdAt", "updatedAt")
				.verify();
	}
}