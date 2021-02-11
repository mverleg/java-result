package nl.markv.result;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ResultTest {

	@Nested
	class Attempting {

		private double fail() throws Exception {
			throw new Exception("checked");
		}

		@Test
		void success() {
			Result<String, Exception> ok = Result.attempt(() -> "hello");
			assert "hello".equals(ok.getOrThrow());
		}

		@Test
		void successException() {
			Result<Exception, Exception> ok = Result.attempt(() -> new IllegalStateException("returned exception"));
			assert ok.getOrThrow() instanceof IllegalStateException;
			assert "returned exception".equals(ok.getOrThrow().getMessage());
		}

		@Test
		void uncheckedFailure() {
			Result<Integer, Exception> err = Result.attempt(() -> {
				throw new IllegalStateException("failed attempt");
			});
			assert err.getErrOrThrow() instanceof IllegalStateException;
			assert "failed attempt".equals(err.getErrOrThrow().getMessage());
		}

		@Test
		void checkedFailure() {
			Result<Double, Exception> err = Result.attempt(this::fail);
			assert Exception.class.equals(err.getErrOrThrow().getClass());
			assert "checked".equals(err.getErrOrThrow().getMessage());
		}
	}
}
