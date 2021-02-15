package nl.markv.result;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
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
	class Mapping {
		@Test
		void okMap() {
			Result<Integer, Integer> err1 = Err.of(2);
			Result<String, Integer> err2 = err1.map(nr -> String.valueOf(2 * nr));
			assert err2.getErrOrThrow() == 2;
		}

		@Test
		void errMap() {
			Result<Integer, Integer> err1 = Err.of(2);
			Result<Integer, String> err2 = err1.mapErr(nr -> String.valueOf(2 * nr));
			assert "4".equals(err2.getErrOrThrow());
		}
	}

	@Nested
	class Alternative {
		Result<Double, Integer> result = Result.err(1);


		@Test
		void orOk() {
			assert result.okOr(2.0) == 2.0;
			assert result.okOr(() -> 2.0) == 2.0;
		}

		@Test
		void orErr() {
			assert result.errOr(2) == 1;
			assert result.errOr(() -> 2) == 1;
		}
	}

	@Nested
	class Adapt {
		@Test
		void changeOkType() {
			Result<String, Integer> err1 = Err.of(1);
			Result<Integer, Integer> err2 = err1.adaptOk();
			assert err2.getErrOrThrow() == 1;
		}

		@Test
		void changeErrType() {
			Result<Integer, Integer> err = Err.of(1);
			assertThrows(WrongResultVariantException.class, err::adaptErr);
		}
	}

	@Nested
	class Without {
		private final Result<String, Integer> result = Err.of(2);

		@Test
	    void withoutOk() {
			Optional<Integer> option = result.withoutOk();
			assert option.isPresent();
			assert option.get() == 2;
		}

		@Test
		void withoutErr() {
			Optional<String> option = result.withoutErr();
			assert option.isEmpty();
		}
	}

	@Nested
	class And {
		private final Result<String, Integer> result = Err.of(2);

		@Test
		void andOk() {
			var res = result.and(Ok.of(1));
			assert result.getErrOrThrow() == 2;
		}

		@Test
		void andOkLazy() {
			var res = result.and(() -> {
					throw new AssertionError();
			});
			assert result.getErrOrThrow() == 2;
		}

		@Test
		void andErr() {
			var res = result.and(Err.of(1));
			assert result.getErrOrThrow() == 2;
		}

		@Test
		void andErrLazy() {
			var res = result.and(() -> {
				throw new AssertionError();
			});
			assert result.getErrOrThrow() == 2;
		}
	}

	@Nested
	class Or {
		private final Result<String, Integer> result = Err.of(2);

		@Test
		void orOk() {
			var res = result.or(Ok.of("hi"));
			assert Ok.of("hi").equals(res);
		}

		@Test
		void orOkLazy() {
			var res = result.or(() -> Ok.of("hi"));
			assert Ok.of("hi").equals(res);
		}

		@Test
		void orErr() {
			var res = result.or(Err.of("err"));
			assert Err.of("err").equals(res);
		}

		@Test
		void orErrLazy() {
			var res = result.or(() -> Err.of("err"));
			assert Err.of("err").equals(res);
		}
	}

	@Nested
	class Contains {
		@Test
		void containOk() {
			assert !Err.of(2).contains(null);
			assert !Err.of(2).contains(2);
		}

		@Test
		void doesContainErr() {
			assert Err.of(2).containsErr(2);
			assert Err.of("hello").containsErr("hello");
		}

		@SuppressWarnings("ConstantConditions")
		@Test
		void doesNotContainErr() {
			assert !Err.of(2).containsErr(null);
			assert !Err.of(2).containsErr(3);
			assert !Err.of("hello").containsErr("bye");
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
	class Sequence {
		private final Result<String, Integer> result = Err.of(2);

		@Test
		void iterator() {
			Iterator<String> iterator = result.iterator();
			assert !iterator.hasNext();
		}

		@Test
		void forLoop() {
			int count = 0;
			for (String ignored : result) {
				count++;
			}
			assert count == 0;
		}

		@Test
		void stream() {
			List<String> list = result.stream().collect(toList());
			assert list.isEmpty();
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
