package nl.markv.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SanityCheck {

	@Test
	void assertionsOn() {
		assertThrows(AssertionError.class, () -> {
			assert false;
		}, "assertions must be enabled");
	}
}
