package pl.marcinm312.springdatasecurityex.user.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class UserEntityTest {

	@Test
	void equalsHashCode_differentCases() {
		EqualsVerifier.forClass(UserEntity.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
				.verify();
	}
}