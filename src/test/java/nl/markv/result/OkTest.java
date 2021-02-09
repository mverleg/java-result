package nl.markv.result;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OkTest {

	//TODO @mark: test Result.ok()
	//TODO @mark: test subtypes (i.e. Ok<Integer, ?> <- Ok<Number, ?>

	@Nested
	class CreateOkOf {
		@Test
		void string() {
			Result<String, String> res = Ok.of("hello");
			assert res.isOk();
			//noinspection ConstantConditions
			if (res instanceof Ok<String, ?> ok) {
				assert "hello".equals(ok.get());
			} else {
				throw new AssertionError();
			}
		}

		@Test
		void number() {
			Result<Integer, String> res = Ok.of(1);
			assert res.isOk();
			//noinspection ConstantConditions
			if (res instanceof Ok<Integer, ?> ok) {
				assert 1 == ok.get();
			} else {
				throw new AssertionError();
			}
		}

		@Test
		void nested() {
			Result<Result<Integer, String>, String> res = Ok.of(Ok.of(1));
			assert res.isOk();
			//noinspection ConstantConditions
			if (res instanceof Ok<Result<Integer, String>, ?> ok1) {
				if (ok1.get() instanceof Ok<Integer, ?> ok2) {
					assert 1 == ok2.get();
				} else {
					throw new AssertionError();
				}
			} else {
				throw new AssertionError();
			}
		}

		@Test
		void inferType() {
			var res = Ok.of(1.0);
			assert res.isOk();
			//noinspection ConstantConditions
			if (res instanceof Ok<Double, ?> ok) {
				assert 1.0 == ok.get();
			} else {
				throw new AssertionError();
			}
		}
	}

}
