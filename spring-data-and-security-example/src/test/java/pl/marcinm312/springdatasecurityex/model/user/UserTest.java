package pl.marcinm312.springdatasecurityex.model.user;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	void equalsHashCode_differentCases() {
		EqualsVerifier.forClass(User.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
				.verify();
	}
}