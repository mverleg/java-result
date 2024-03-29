package nl.markv.result;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static nl.markv.result.Result.err;
import static nl.markv.result.Result.ok;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErrTest {

	static class Types {
		static class Super {}

		static class Sub extends Super {}

		final Err<Integer, Integer> bareErr = err(1);
		final Result<Integer, Integer> resultErr = bareErr;
		final Err<Integer, Integer> newBareErr = (Err<Integer, Integer>) resultErr;

		final List<Err<?, Integer>> listOfErr = List.of(err(1));
		final List<? extends Result<?, Integer>> listOfResultErr = listOfErr;
		final int listErrValue = listOfResultErr.get(0).getErrOrThrow();

		final Result<?, Sub> subErr = err(new Sub());
		final Result<?, ? extends Super> superErr = subErr;
		@SuppressWarnings("unused")
		final Super value = superErr.getErrOrThrow();
	}

	@Test
	void types() {
		var types = new Types();  // Force load static nested class.
		assert 1 == types.newBareErr.getErrOrThrow();
		assert 1 == types.listErrValue;
	}

	@Nested
	class Create {
		@Test
		void string() {
			Result<Object, String> res = Err.of("hello");
			assert !res.isOk();
			assert res.isErr();
			if (res instanceof Err<?, String> err) {
				assert "hello".equals(err.get());
			} else {
				throw new AssertionError();
			}
		}

		@Test
		void number() {
			Result<Object, Integer> res = err(1);
			assert res.isErr();
			if (res instanceof Err<?, Integer> err) {
				assert 1 == err.get();
			} else {
				throw new AssertionError();
			}
		}

		@Test
		void nested() {
			Result<String, Result<String, Integer>> res = err(err(1));
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
			Result<Object, Double> res = err(1.0);
			assert res.isErr();
			if (res instanceof Err<?, Double> err) {
				assert 1.0 == err.get();
			} else {
				throw new AssertionError();
			}
		}

		@Test
		@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
		void notNull() {
			assertThrows(NullPointerException.class, () -> err(null));
			assertThrows(NullPointerException.class, () -> err(null));
		}
	}

	@Nested
	class GetOrThrow {
		@Test
		void getErr() {
			var res = err("hello");
			assert "hello".equals(res.getErrOrThrow());
			assert "hello".equals(res.getErrOrThrow("my error"));
			assert "hello".equals(res.getErrOrThrow(IllegalStateException::new));
		}

		@SuppressWarnings("deprecation")
		@Test
		void getOk() {
			var res = err("hello");
			assertThrows(WrongResultVariantException.class, res::getOrThrow);
			var ex = assertThrows(WrongResultVariantException.class, () -> res.getOrThrow("my error"));
			assert "my error".equals(ex.getMessage());
			assertThrows(IllegalStateException.class, () -> res.getOrThrow(IllegalStateException::new));
		}

		@Test
		@SuppressWarnings({"ConstantConditions", "deprecation"})
		void nonNull() {
			var res = err("hello");
			assertThrows(NullPointerException.class, () -> res.getOrThrow((String) null));
			assertThrows(NullPointerException.class, () -> res.getOrThrow((Supplier<RuntimeException>) null));
			assertThrows(NullPointerException.class, () -> res.getOrThrow(TestUtil::nullSupplier));
		}
	}

	@Nested
	class Mapping {
		@Test
		void okMap() {
			Result<Integer, Integer> err1 = err(2);
			var err2 = err1.map(nr -> String.valueOf(2 * nr));
			assert err2.getErrOrThrow() == 2;
		}

		@Test
		void errMap() {
			var err1 = err(2);
			var err2 = err1.mapErr(nr -> String.valueOf(2 * nr));
			assert "4".equals(err2.getErrOrThrow());
		}

		@Test
		void okFlatMapOk() {
			Result<Integer, Integer> err1 = err(2);
			var err2 = err1.flatMap(nr -> ok(String.valueOf(2 * nr)));
			assert err2.getErrOrThrow() == 2;
		}

		@Test
		void okFlatMapErr() {
			Result<Integer, Integer> err1 = err(2);
			var err2 = err1.flatMap(nr -> err(2 * nr));
			assert err2.getErrOrThrow() == 2;
		}

		@Test
		void errFlatMapOk() {
			var err = err(2);
			var ok = err.flatMapErr(nr -> ok(String.valueOf(2 * nr)));
			assert "4".equals(ok.getOrThrow());
		}

		@Test
		void errFlatMapErr() {
			var err1 = err(2);
			var err2 = err1.flatMapErr(nr -> err(String.valueOf(2 * nr)));
			assert "4".equals(err2.getErrOrThrow());
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			var res = err("hello");
			assertThrows(NullPointerException.class, () -> res.map(null));
			assertThrows(NullPointerException.class, () -> res.mapErr(null));
			assertThrows(NullPointerException.class, () -> res.mapErr(TestUtil::nullFunction));
			assertThrows(NullPointerException.class, () -> res.flatMap(null));
			assertThrows(NullPointerException.class, () -> res.flatMapErr(null));
			assertThrows(NullPointerException.class, () -> res.flatMapErr(TestUtil::nullFunction));
		}
	}

	@Nested
	class IfOkErr {
		@Test
		void ifErr() {
			var toggle = new TestUtil.Toggle();
			err(2).ifErr(value -> {
				assert value == 2;
				toggle.turnOn();
			});
			assert toggle.isOn();
		}

		@Test
		void ifOk() {
			err(2).ifOk(TestUtil::failIfCalled);
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			var res = err("hello");
			assertThrows(NullPointerException.class, () -> res.ifOk(null));
			assertThrows(NullPointerException.class, () -> res.ifErr(null));
		}
	}

	@Nested
	class IfEither {
		@Test
		void ifEither() {
			var toggle = new TestUtil.Toggle();
			err(2).ifEither(
					TestUtil::failIfCalled,
					err -> toggle.turnOn()
			);
			assert toggle.isOn();
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			var res = err("hello");
			assertThrows(NullPointerException.class, () -> res.ifEither(null, ignored -> {}));
			assertThrows(NullPointerException.class, () -> res.ifEither(ignored -> {}, null));
		}
	}

	@Nested
	class Branch {
		@Test
		void toNumber() {
			int result = err(2).branch(
					TestUtil::failIfCalled,
					err -> err * 3
			);
			assert result == 6;
		}

		@Test
		void toResult() {
			var result = err(2).branch(
					TestUtil::failIfCalled,
					err -> ok("ok!")

			);
			assert result.isOk();
			assert "ok!".equals(result.getOrThrow());
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			var res = err("hello");
			assertThrows(NullPointerException.class, () -> res.branch(null, ignored -> 1));
			assertThrows(NullPointerException.class, () -> res.branch(ignored -> 1, null));
			assertThrows(NullPointerException.class, () -> res.branch(ignored -> 1, TestUtil::nullFunction));
		}
	}

	@Nested
	class Recover {
		@Test
		void recovery() {
			var result = err(2).recover(err -> "hello");
			assert "hello".equals(result);
		}

		@Test
		@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
		void nonNull() {
			var input = ok("hello");
			assertThrows(NullPointerException.class, () -> input.recover(null));
		}
	}

	@Nested
	class Alternative {
		final Result<Double, Integer> result = err(1);

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
			assert result.okOrNullable(TestUtil::nullSupplier) == null;
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
			assert result.errOrNullable(TestUtil::nullSupplier) == 1;
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void errOrNull() {
			assert result.errOrNull() == 1;
		}

		@Test
		@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
		void nonNull() {
			var input = err("hello");
			assertThrows(NullPointerException.class, () -> input.okOr((String)null));
			assertThrows(NullPointerException.class, () -> input.okOr((Supplier<String>) null));
			assertThrows(NullPointerException.class, () -> input.okOr(TestUtil::nullSupplier));
			assertThrows(NullPointerException.class, () -> input.okOrNullable(null));
			assertThrows(NullPointerException.class, () -> input.errOr((String)null));
			assertThrows(NullPointerException.class, () -> input.errOr((Supplier<String>) null));
			assertThrows(NullPointerException.class, () -> input.errOrNullable((Supplier<String>) null));
		}

		@Test
		void isLazy() {
			result.errOr(TestUtil::failIfCalled);
			result.errOrNullable(TestUtil::failIfCalled);
		}
	}

	@Nested
	class Adapt {
		@Test
		void changeErrType() {
			var err = err(1);
			assertThrows(WrongResultVariantException.class, err::adaptErr);
		}

		@Test
		void changeOkType() {
			var err1 = err(1);
			var err2 = err1.adaptOk();
			assert err2.getErrOrThrow() == 1;
		}
	}

	@Nested
	class Without {
		final Result<String, Integer> result = err(2);

		@Test
		void withoutErr() {
			Optional<String> option = result.withoutErr();
			assert option.isEmpty();
		}

		@Test
	    void withoutOk() {
			Optional<Integer> option = result.withoutOk();
			assert option.isPresent();
			assert option.get() == 2;
		}
	}

	@Nested
	class And {
		final Result<String, Integer> input = err(2);

		@Test
		void andOk() {
			var res = input.and(ok(1));
			assert res.getErrOrThrow() == 2;
		}

		@Test
		void andOkLazy() {
			var res = input.and(TestUtil::failIfCalled);
			assert res.getErrOrThrow() == 2;
		}

		@Test
		void andErr() {
			var res = input.and(err(1));
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
		final Result<String, Integer> input = err(2);

		@Test
		void orOk() {
			var res = input.or(ok("hi"));
			assert ok("hi").equals(res);
		}

		@Test
		void orOkLazy() {
			var res = input.or(() -> ok("hi"));
			assert ok("hi").equals(res);
		}

		@Test
		void orErr() {
			var res = input.or(err("err"));
			assert err("err").equals(res);
		}

		@Test
		void orErrLazy() {
			var res = input.or(() -> err("err"));
			assert err("err").equals(res);
		}

		@Test
		@SuppressWarnings({"ConstantConditions"})
		void nonNull() {
			assertThrows(NullPointerException.class, () -> input.or((Result<String, String>) null));
			assertThrows(NullPointerException.class, () -> input.or((Supplier<Result<String, String>>) null));
			assertThrows(NullPointerException.class, () -> input.or((Supplier<Result<String, String>>) TestUtil::nullSupplier));
		}
	}

	@Nested
	class Contains {
		@Test
		void containOk() {
			assert !err(2).contains(null);
			assert !err(2).contains(2);
		}

		@Test
		void doesContainErr() {
			assert err(2).containsErr(2);
			assert err("hello").containsErr("hello");
		}

		@SuppressWarnings("ConstantConditions")
		@Test
		void doesNotContainErr() {
			assert !err(2).containsErr(null);
			assert !err(2).containsErr(3);
			assert !err("hello").containsErr("bye");
		}
	}

	@Nested
	class Matches {
		@Test
		void errTrue() {
			assert err(2).errMatches(ok -> ok == 2);
			assert err("HELLO").errMatches("hello"::equalsIgnoreCase);
		}

		@Test
		void errFalse() {
			assert !err(2).errMatches(ok -> ok == 3);
			assert !err("HELLO").errMatches("hello"::equals);
		}

		@Test
		void okMatch() {
			assert !ok(2).errMatches(err -> true);
			assert !Ok.<Integer, Integer>of(2).errMatches(err -> err == 2);
			//noinspection ResultOfMethodCallIgnored
			ok(2).errMatches(TestUtil::failIfCalled);
		}

		@Test
		@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
		void nonNull() {
			var input = err("hello");
			assertThrows(NullPointerException.class, () -> input.matches(null));
			assertThrows(NullPointerException.class, () -> input.errMatches(null));
		}
	}

	@Nested
	class Unified {
		@Test
		void get() {
			TestData content = new TestData(1);
			var err = err(content);
			assert content == err.getUnified();
		}
	}

	@Nested
	class Sequence {
		final Result<String, Integer> result = err(2);

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
			var err = err("hello");
			assert err.equals(err);
			assert err("hello").equals(err("hello"));
			assert !err("hello").equals(err("bye"));
			assert !err("hello").equals(err(1));
			assert !err("bye").equals(ok("hello"));
			assert !err("hello").equals(null);
		}

		@Test
		void testHashCode() {
			assert err("hello").hashCode() == err("hello").hashCode();
			assert err("hello").hashCode() != err("bye").hashCode();
			assert err("hello").hashCode() != err(1).hashCode();
			assert err("hello").hashCode() != ok("hello").hashCode();
		}

		@Test
		void testToString() {
			assert "Err(1)".equals(err(1).toString());
			assert "Err(hello)".equals(err("hello").toString());
		}
	}
}
