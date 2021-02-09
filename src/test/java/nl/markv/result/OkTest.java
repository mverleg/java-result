package nl.markv.result;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OkTest {

	@Nested
	class Create {
		@Test
		void ofString() {
			Result<String, String> ok = Ok.of("hello");
			assert ok.isOk();
		}

		@Test
		void ofNumber() {
			Result<Integer, String> ok = Ok.of(1);
			assert ok.isOk();
		}

		@Test
		void nested() {
			Result<Result<Integer, String>, String> ok = Ok.of(Ok.of(1));
			assert ok.isOk();
		}

		@Test
		void inferType() {
			var ok = Ok.of(1.0);
			assert ok.isOk();
		}
	}
}
