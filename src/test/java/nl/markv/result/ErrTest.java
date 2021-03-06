package nl.markv.result;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertThrows;

//TODO @mark: test null handling of all methods
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
			var ex = assertThrows(WrongResultVariantException.class, () -> res.getOrThrow("my error"));
			assert "my error".equals(ex.getMessage());
			assertThrows(IllegalStateException.class, () -> res.getOrThrow(IllegalStateException::new));
		}

		@Test
		void getErr() {
			Result<String, String> res = Err.of("hello");
			assert "hello".equals(res.getErrOrThrow());
			assert "hello".equals(res.getErrOrThrow("my error"));
			assert "hello".equals(res.getErrOrThrow(IllegalStateException::new));
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			//TODO @mark: Ok -> Err
			Result<String, String> res = Err.of("hello");
			assertThrows(NullPointerException.class, () -> res.getOrThrow((String)null));
			assertThrows(NullPointerException.class, () -> res.getOrThrow(() -> null));
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

		@Test
		void okFlatMapOk() {
			Result<Integer, Integer> err1 = Err.of(2);
			Result<String, Integer> err2 = err1.flatMap(nr -> Ok.of(String.valueOf(2 * nr)));
			assert err2.getErrOrThrow() == 2;
		}

		@Test
		void okFlatMapErr() {
			Result<Integer, Integer> err1 = Err.of(2);
			Result<String, Integer> err2 = err1.flatMap(nr -> Err.of(2 * nr));
			assert err2.getErrOrThrow() == 2;
		}

		@Test
		void errFlatMapOk() {
			Result<String, Integer> err = Err.of(2);
			Result<String, Integer> ok = err.flatMapErr(nr -> Ok.of(String.valueOf(2 * nr)));
			assert "4".equals(ok.getOrThrow());
		}

		@Test
		void errFlatMapErr() {
			Result<Integer, Integer> err1 = Err.of(2);
			Result<Integer, String> err2 = err1.flatMapErr(nr -> Err.of(String.valueOf(2 * nr)));
			assert "4".equals(err2.getErrOrThrow());
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			Result<String, String> res = Err.of("hello");
			assertThrows(NullPointerException.class, () -> res.map(null));
			assertThrows(NullPointerException.class, () -> res.mapErr(null));
			assertThrows(NullPointerException.class, () -> res.mapErr(ignored -> null));
			assertThrows(NullPointerException.class, () -> res.flatMap(null));
			assertThrows(NullPointerException.class, () -> res.flatMapErr(null));
			assertThrows(NullPointerException.class, () -> res.flatMapErr(ignored -> null));
		}
	}

	@Nested
	class IfOkErr {
		@Test
		void ifOk() {
			Err.of(2).ifOk(TestUtil::failIfCalled);
		}

		@Test
		void ifErr() {
			var toggle = new TestUtil.Toggle();
			Err.of(2).ifErr(value -> {
				assert value == 2;
				toggle.turnOn();
			});
			assert toggle.isOn();
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			Result<String, String> res = Err.of("hello");
			assertThrows(NullPointerException.class, () -> res.ifOk(null));
			assertThrows(NullPointerException.class, () -> res.ifErr(null));
		}
	}

	@Nested
	class IfEither {
		@Test
		void ifEither() {
			var toggle = new TestUtil.Toggle();
			Err.of(2).ifEither(
					TestUtil::failIfCalled,
					err -> toggle.turnOn()
			);
			assert toggle.isOn();
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			Result<String, String> res = Err.of("hello");
			assertThrows(NullPointerException.class, () -> res.ifEither(null, ignored -> {}));
			assertThrows(NullPointerException.class, () -> res.ifEither(ignored -> {}, null));
		}
	}

	@Nested
	class Branch {
		@Test
		void toNumber() {
			int result = Err.of(2).branch(
					TestUtil::failIfCalled,
					err -> err * 3
			);
			assert result == 6;
		}

		@Test
		void toResult() {
			var result = Err.of(2).branch(
					TestUtil::failIfCalled,
					err -> Ok.of("ok!")

			);
			assert result.isOk();
			assert "ok!".equals(result.getOrThrow());
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			Result<String, String> res = Err.of("hello");
			assertThrows(NullPointerException.class, () -> res.branch(null, ignored -> 1));
			assertThrows(NullPointerException.class, () -> res.branch(ignored -> 1, null));
			assertThrows(NullPointerException.class, () -> res.branch(ignored -> 1, ignored -> null));
		}
	}

	@Nested
	class Solve {
		@Test
		void solve() {
			var result = Err.of(2).solve(err -> "hello");
			assert "hello".equals(result);
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			Result<String, String> input = Ok.of("hello");
			assertThrows(NullPointerException.class, () -> input.solve(null));
		}
	}

	@Nested
	class Alternative {
		//TODO @mark: test that suppliers aren't called if not needed
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

		@Test
		@SuppressWarnings("ConstantConditions")
		void okOrNullable() {
			assert result.okOrNullable(2.0) == 2.0;
			assert result.okOrNullable((Double)null) == null;
			assert result.okOrNullable(() -> 2.0) == 2.0;
			assert result.okOrNullable(() -> null) == null;
		}

		@Test
		void okOrNull() {
			assert result.okOrNull() == null;
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void errOrNullable() {
			assert result.errOrNullable(2) == 1;
			assert result.errOrNullable((Integer)null) == 1;
			assert result.errOrNullable(() -> 2) == 1;
			assert result.errOrNullable(() -> null) == 1;
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void errOrNull() {
			assert result.errOrNull() == 1;
		}

		@Test
		@SuppressWarnings({"ConstantConditions"})
		void nonNull() {
			Result<String, String> input = Err.of("hello");
			assertThrows(NullPointerException.class, () -> input.okOr((String)null));
			assertThrows(NullPointerException.class, () -> input.okOr((Supplier<String>) null));
			assertThrows(NullPointerException.class, () -> input.okOr(() -> null));
			assertThrows(NullPointerException.class, () -> input.okOrNullable((Supplier<String>) null));
			assertThrows(NullPointerException.class, () -> input.errOr((String)null));
			assertThrows(NullPointerException.class, () -> input.errOr((Supplier<String>) null));
			assertThrows(NullPointerException.class, () -> input.errOrNullable((Supplier<String>) null));
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
		private final Result<String, Integer> input = Err.of(2);

		@Test
		void andOk() {
			var res = input.and(Ok.of(1));
			assert res.getErrOrThrow() == 2;
		}

		@Test
		void andOkLazy() {
			var res = input.and(TestUtil::failIfCalled);
			assert res.getErrOrThrow() == 2;
		}

		@Test
		void andErr() {
			var res = input.and(Err.of(1));
			assert res.getErrOrThrow() == 2;
		}

		@Test
		void andErrLazy() {
			var res = input.and(TestUtil::failIfCalled);
			assert res.getErrOrThrow() == 2;
		}
	}

	@Nested
	class Or {
		private final Result<String, Integer> input = Err.of(2);

		@Test
		void orOk() {
			var res = input.or(Ok.of("hi"));
			assert Ok.of("hi").equals(res);
		}

		@Test
		void orOkLazy() {
			var res = input.or(() -> Ok.of("hi"));
			assert Ok.of("hi").equals(res);
		}

		@Test
		void orErr() {
			var res = input.or(Err.of("err"));
			assert Err.of("err").equals(res);
		}

		@Test
		void orErrLazy() {
			var res = input.or(() -> Err.of("err"));
			assert Err.of("err").equals(res);
		}

		@Test
		@SuppressWarnings({"ConstantConditions"})
		void nonNull() {
			assertThrows(NullPointerException.class, () -> input.or((Result<String, String>) null));
			assertThrows(NullPointerException.class, () -> input.or((Supplier<Result<String, String>>) null));
			assertThrows(NullPointerException.class, () -> input.or((Supplier<Result<String, String>>) () -> null));
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
	class Matches {
		@Test
		void ok() {
			assert !Ok.of(2).errMatches(err -> true);
			assert !Ok.<Integer, Integer>of(2).errMatches(err -> err == 2);
			//noinspection ResultOfMethodCallIgnored
			Ok.of(2).errMatches(TestUtil::failIfCalled);
		}

		@Test
		void errTrue() {
			assert Err.of(2).errMatches(ok -> ok == 2);
			assert Err.of("HELLO").errMatches("hello"::equalsIgnoreCase);
		}

		@Test
		void errFalse() {
			assert !Err.of(2).errMatches(ok -> ok == 3);
			assert !Err.of("HELLO").errMatches("hello"::equals);
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			Result<String, String> input = Err.of("hello");
			assertThrows(NullPointerException.class, () -> input.matches(null));
			assertThrows(NullPointerException.class, () -> input.errMatches(null));
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
			assert Err.of("hello").hashCode() != Ok.of("hello").hashCode();
		}

		@Test
		void testToString() {
			assert "Err(1)".equals(Err.of(1).toString());
			assert "Err(hello)".equals(Err.of("hello").toString());
		}
	}
}
