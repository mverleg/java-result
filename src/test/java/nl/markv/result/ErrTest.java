package nl.markv.result;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

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

		@Test
		@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
		void notNull() {
			assertThrows(NullPointerException.class, () -> Result.err(null));
			assertThrows(NullPointerException.class, () -> Err.of(null));
		}
	}


	@Nested
	class GetOrThrow {
		@Test
		void getOk() {
			Result<String, String> res = Err.of("hello");
			assertThrows(WrongResultVariantException.class, res::getOrThrow);
			assertThrows(IllegalStateException.class, () -> res.getOrThrow(IllegalStateException::new));
		}

		@Test
		void getErr() {
			Result<String, String> res = Err.of("hello");
			assert "hello".equals(res.getErrOrThrow());
			assert "hello".equals(res.getErrOrThrow(IllegalStateException::new));
		}
	}

	@Nested
	class Unified {
		@Test
		void get() {
			TestData content = new TestData(1);
			Result<String, TestData> err = Err.of(content);
			assert content == err.getUnified();
		}
	}

	@Nested
	class ObjectMethods {

		@Test
		@SuppressWarnings({"EqualsWithItself", "EqualsBetweenInconvertibleTypes", "ConstantConditions"})
		void testEquals() {
			Result<String, String> err = Err.of("hello");
			assert err.equals(err);
			assert Err.of("hello").equals(Err.of("hello"));
			assert !Err.of("hello").equals(Err.of("bye"));
			assert !Err.of("hello").equals(Err.of(1));
			assert !Err.of("bye").equals(Ok.of("hello"));
			assert !Err.of("hello").equals(null);
		}

		@Test
		void testHashCode() {
			assert Err.of("hello").hashCode() == Err.of("hello").hashCode();
			assert Err.of("hello").hashCode() != Err.of("bye").hashCode();
			assert Err.of("hello").hashCode() != Err.of(1).hashCode();
			assert Err.of("bye").hashCode() != Ok.of("hello").hashCode();
		}

		@Test
		void testToString() {
			assert "Err(1)".equals(Err.of(1).toString());
			assert "Err(hello)".equals(Err.of("hello").toString());
		}
	}
}
