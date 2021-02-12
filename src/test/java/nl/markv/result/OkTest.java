package nl.markv.result;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OkTest {

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

	@Nested
	class GetOrThrow {
		@Test
		void getOk() {
			Result<String, String> res = Ok.of("hello");
			assert "hello".equals(res.getOrThrow());
			assert "hello".equals(res.getOrThrow(IllegalStateException::new));
		}

		@Test
		void getErr() {
			Result<String, String> res = Ok.of("hello");
			assertThrows(WrongResultVariantException.class, res::getErrOrThrow);
			assertThrows(IllegalStateException.class, () -> res.getErrOrThrow(IllegalStateException::new));
		}
	}

	@Nested
	class Adapt {
		@Test
		void changeErrType() {
			Result<Integer, String> ok1 = Ok.of(1);
			Result<Integer, Integer> ok2 = ok1.adaptErr();
			assert ok2.getOrThrow() == 1;
		}
	}

	@Nested
	class Unified {
		@Test
		void get() {
			TestData content = new TestData(1);
			Result<TestData, String> ok = Ok.of(content);
			assert content == ok.getUnified();
		}
	}

	@Nested
	class ObjectMethods {

		@Test
		@SuppressWarnings({"EqualsWithItself", "EqualsBetweenInconvertibleTypes", "ConstantConditions"})
		void testEquals() {
			Result<String, String> ok = Ok.of("hello");
			assert ok.equals(ok);
			assert Ok.of("hello").equals(Ok.of("hello"));
			assert !Ok.of("hello").equals(Ok.of("bye"));
			assert !Ok.of("hello").equals(Ok.of(1));
			assert !Ok.of("hello").equals(Err.of("bye"));
			assert !Ok.of("hello").equals(null);
		}

		@Test
		void testHashCode() {
			assert Ok.of("hello").hashCode() == Ok.of("hello").hashCode();
			assert Ok.of("hello").hashCode() != Ok.of("bye").hashCode();
			assert Ok.of("hello").hashCode() != Ok.of(1).hashCode();
			assert Ok.of("hello").hashCode() != Err.of("bye").hashCode();
		}

		@Test
		void testToString() {
			assert "Ok(1)".equals(Ok.of(1).toString());
			assert "Ok(hello)".equals(Ok.of("hello").toString());
		}
	}
}
