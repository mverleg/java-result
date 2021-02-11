package nl.markv.result;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OkTest {

	//TODO @mark: test toString, hashCode, equals
	//TODO @mark: test subtypes (i.e. Ok<Integer, ?> <- Ok<Number, ?>

	@Nested
	class Create {
		@Test
		void string() {
			Result<String, String> res = Ok.of("hello");
			assert res.isOk();
			assert !res.isErr();
			//noinspection ConstantConditions
			if (res instanceof Ok<String, ?> ok) {
				assert "hello".equals(ok.get());
			} else {
				throw new AssertionError();
			}
		}

		@Test
		void number() {
			Result<Integer, String> res = Result.ok(1);
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
			Result<Result<Integer, String>, String> res = Result.ok(Ok.of(1));
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
			var res = Result.ok(1.0);
			assert res.isOk();
			//noinspection ConstantConditions
			if (res instanceof Ok<Double, ?> ok) {
				assert 1.0 == ok.get();
			} else {
				throw new AssertionError();
			}
		}

		@Test
		@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
		void notNull() {
			assertThrows(NullPointerException.class, () -> Result.ok(null));
			assertThrows(NullPointerException.class, () -> Ok.of(null));
		}
	}
}
