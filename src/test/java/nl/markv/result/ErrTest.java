package nl.markv.result;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ErrTest {

	@Nested
	class Create {
		@Test
		void string() {
			Result<String, String> res = Err.of("hello");
			assert !res.isOk();
			assert res.isErr();
			//noinspection ConstantConditions
			if (res instanceof Err<?, String> err) {
				assert "hello".equals(err.get());
			} else {
				throw new AssertionError();
			}
		}

		@Test
		void number() {
			Result<String, Integer> res = Result.err(1);
			assert res.isErr();
			//noinspection ConstantConditions
			if (res instanceof Err<?, Integer> err) {
				assert 1 == err.get();
			} else {
				throw new AssertionError();
			}
		}

		@Test
		void nested() {
			Result<String, Result<String, Integer>> res = Result.err(Err.of(1));
			assert res.isErr();
			//noinspection ConstantConditions
			if (res instanceof Err<?, Result<String, Integer>> err1) {
				if (err1.get() instanceof Err<?, Integer> err2) {
					assert 1 == err2.get();
				} else {
					throw new AssertionError();
				}
			} else {
				throw new AssertionError();
			}
		}

		@Test
		void inferType() {
			var res = Result.err(1.0);
			assert res.isErr();
			//noinspection ConstantConditions
			if (res instanceof Err<?, Double> err) {
				assert 1.0 == err.get();
			} else {
				throw new AssertionError();
			}
		}
	}
}
