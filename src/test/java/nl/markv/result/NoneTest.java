package nl.markv.result;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NoneTest {

	@Nested
	class Create {
		@Test
	    void singleInstance() {
		    assert None.get() == None.get();
		}
	}
}
