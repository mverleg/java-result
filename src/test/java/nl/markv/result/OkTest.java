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

class OkTest {

	static class Types {
		static class Super {}

		static class Sub extends Super {}

		final Ok<Integer, Integer> bareOk = ok(1);
		final Result<Integer, Integer> resultOk = bareOk;
		final Ok<Integer, Integer> newBareErr = (Ok<Integer, Integer>) resultOk;

		final List<Ok<Integer, ?>> listOfOk = List.of(ok(1));
		final List<? extends Result<Integer, ?>> listOfResultOk = listOfOk;
		final int listErrValue = listOfResultOk.get(0).getOrThrow();

		final Result<Sub, ?> subOk = ok(new Sub());
		final Result<? extends Super, ?> superOk = subOk;
		@SuppressWarnings("unused")
		final Super value = superOk.getOrThrow();
	}

	@Test
	void types() {
		var types = new Types();  // Force load static nested class.
		assert 1 == types.newBareErr.getOrThrow();
		assert 1 == types.listErrValue;
	}

	@Nested
	class Create {
		@Test
		void string() {
			var res = Ok.of("hello");
			assert res.isOk();
			assert !res.isErr();
			if (res instanceof Ok<String, ?> ok) {
				assert "hello".equals(ok.get());
			} else {
				throw new AssertionError();
			}
		}

		@Test
		void number() {
			var res = ok(1);
			assert res.isOk();
			if (res instanceof Ok<Integer, ?> ok) {
				assert 1 == ok.get();
			} else {
				throw new AssertionError();
			}
		}

		@Test
		void nested() {
			Result<Result<Integer, String>, String> res = ok(ok(1));
			assert res.isOk();
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
			var res = ok(1.0);
			assert res.isOk();
			if (res instanceof Ok<Double, ?> ok) {
				assert 1.0 == ok.get();
			} else {
				throw new AssertionError();
			}
		}

		@Test
		@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
		void notNull() {
			assertThrows(NullPointerException.class, () -> ok(null));
			assertThrows(NullPointerException.class, () -> ok(null));
		}
	}

	@Nested
	class GetOrThrow {
		@Test
		void getOk() {
			var res = ok("hello");
			assert "hello".equals(res.getOrThrow());
			assert "hello".equals(res.getOrThrow("my error"));
			assert "hello".equals(res.getOrThrow(IllegalStateException::new));
		}

		@SuppressWarnings("deprecation")
		@Test
		void getErr() {
			var res = ok("hello");
			assertThrows(WrongResultVariantException.class, res::getErrOrThrow);
			var ex = assertThrows(WrongResultVariantException.class, () -> res.getErrOrThrow("my error"));
			assert "my error".equals(ex.getMessage());
			assertThrows(IllegalStateException.class, () -> res.getErrOrThrow(IllegalStateException::new));
		}

		@Test
		@SuppressWarnings({"ConstantConditions", "deprecation"})
		void nonNull() {
			var res = ok("hello");
			assertThrows(NullPointerException.class, () -> res.getErrOrThrow((String)null));
			assertThrows(NullPointerException.class, () -> res.getErrOrThrow(TestUtil::nullSupplier));
		}
	}

	@Nested
	class Mapping {
		@Test
		void okMap() {
			var ok1 = ok(2);
			var ok2 = ok1.map(nr -> String.valueOf(2 * nr));
			assert "4".equals(ok2.getOrThrow());
		}

		@Test
		void errMap() {
			Result<Integer, Integer> ok1 = ok(2);
			var ok2 = ok1.mapErr(nr -> String.valueOf(2 * nr));
			assert ok2.getOrThrow() == 2;
		}

		@Test
		void okFlatMapOk() {
			var ok1 = ok(2);
			var ok2 = ok1.flatMap(nr -> ok(String.valueOf(2 * nr)));
			assert "4".equals(ok2.getOrThrow());
		}

		@Test
		void okFlatMapErr() {
			var ok = ok(2);
			var err = ok.flatMap(nr -> err(String.valueOf(2 * nr)));
			assert "4".equals(err.getErrOrThrow());
		}

		@Test
		void errFlatMapOk() {
			Result<Integer, Integer> ok1 = ok(2);
			var ok2 = ok1.flatMapErr(nr -> ok(2 * nr));
			assert ok2.getOrThrow() == 2;
		}

		@Test
		void errFlatMapErr() {
			Result<Integer, Integer> ok1 = ok(2);
			var ok2 = ok1.flatMapErr(nr -> err(String.valueOf(2 * nr)));
			assert ok2.getOrThrow() == 2;
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			var res = ok("hello");
			assertThrows(NullPointerException.class, () -> res.map(null));
			assertThrows(NullPointerException.class, () -> res.map(ignored -> null));
			assertThrows(NullPointerException.class, () -> res.mapErr(null));
			assertThrows(NullPointerException.class, () -> res.flatMap(null));
			assertThrows(NullPointerException.class, () -> res.flatMap(ignored -> null));
			assertThrows(NullPointerException.class, () -> res.flatMapErr(null));
		}
	}

	@Nested
	class IfOkErr {
		@Test
	    void ifOk() {
			var toggle = new TestUtil.Toggle();
		    ok(2).ifOk(value -> {
		    	assert value == 2;
				toggle.turnOn();
			});
		    assert toggle.isOn();
		}

		@Test
		void ifErr() {
			ok(2).ifErr(TestUtil::failIfCalled);
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			var res = ok("hello");
			assertThrows(NullPointerException.class, () -> res.ifOk(null));
			assertThrows(NullPointerException.class, () -> res.ifErr(null));
		}
	}
	
	@Nested
	class IfEither {
		@Test
		void ifEither() {
			var toggle = new TestUtil.Toggle();
			ok(2).ifEither(
					ok -> toggle.turnOn(),
					TestUtil::failIfCalled
			);
			assert toggle.isOn();
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			var res = ok("hello");
			assertThrows(NullPointerException.class, () -> res.ifEither(null, ignored -> {}));
			assertThrows(NullPointerException.class, () -> res.ifEither(ignored -> {}, null));
		}
	}

	@Nested
	class Branch {
		@Test
	    void toNumber() {
			int result = ok(2).branch(
					ok -> ok * 3,
					TestUtil::failIfCalled
			);
			assert result == 6;
		}

		@Test
	    void toResult() {
			var result = ok(2).branch(
					ok -> err("wrong!"),
					TestUtil::failIfCalled
			);
			assert result.isErr();
			assert "wrong!".equals(result.getErrOrThrow());
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			var res = ok("hello");
			assertThrows(NullPointerException.class, () -> res.branch(null, ignored -> 1));
			assertThrows(NullPointerException.class, () -> res.branch(ignored -> null, ignored -> 1));
			assertThrows(NullPointerException.class, () -> res.branch(ignored -> 1, null));
		}
	}

	@Nested
	class Solve {
		@Test
	    void solve() {
			var result = ok(2).solve(TestUtil::failIfCalled);
			assert 2 == result;
		}

		@Test
		@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
		void nonNull() {
			var input = ok("hello");
			assertThrows(NullPointerException.class, () -> input.solve(null));
		}
	}

	@Nested
	class Alternative {
		final Result<Integer, Double> result = ok(1);

		@Test
	    void orOk() {
		    assert result.okOr(2) == 1;
		    assert result.okOr(() -> 2) == 1;
		}

		@Test
	    void orErr() {
			assert result.errOr(2.0) == 2.0;
			assert result.errOr(() -> 2.0) == 2.0;
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void okOrNullable() {
			assert result.okOrNullable(2) == 1;
			assert result.okOrNullable((Integer)null) == 1;
			assert result.okOrNullable(() -> 2) == 1;
			assert result.okOrNullable(TestUtil::nullSupplier) == 1;
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void okOrNull() {
			assert result.okOrNull() == 1;
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void errOrNullable() {
			assert result.errOrNullable(2.0) == 2.0;
			assert result.errOrNullable((Double)null) == null;
			assert result.errOrNullable(() -> 2.0) == 2.0;
			assert result.errOrNullable(TestUtil::nullSupplier) == null;
		}

		@Test
		void errOrNull() {
			assert result.errOrNull() == null;
		}

		@Test
		@SuppressWarnings({"ConstantConditions"})
		void nonNull() {
			var input = ok("hello");
			assertThrows(NullPointerException.class, () -> input.okOr((String)null));
			assertThrows(NullPointerException.class, () -> input.okOr((Supplier<String>) null));
			assertThrows(NullPointerException.class, () -> input.okOrNullable((Supplier<String>) null));
			assertThrows(NullPointerException.class, () -> input.errOr((String)null));
			assertThrows(NullPointerException.class, () -> input.errOr((Supplier<String>) null));
			assertThrows(NullPointerException.class, () -> input.errOr(TestUtil::nullSupplier));
			assertThrows(NullPointerException.class, () -> input.errOrNullable((Supplier<Object>) null));
		}

		@Test
		void isLazy() {
			result.okOr(TestUtil::failIfCalled);
			result.okOrNullable(TestUtil::failIfCalled);
		}
	}

	@Nested
	class Adapt {
		@Test
		void changeOkType() {
			var ok = ok(1);
			assertThrows(WrongResultVariantException.class, ok::adaptOk);
		}

		@Test
		void changeErrType() {
			var ok1 = ok(1);
			var ok2 = ok1.adaptErr();
			assert ok2.getOrThrow() == 1;
		}
	}

	@Nested
	class Without {
		final Result<Integer, String> result = ok(2);

		@Test
		void withoutOk() {
			Optional<String> option = result.withoutOk();
			assert option.isEmpty();
		}

		@Test
		void withoutErr() {
			Optional<Integer> option = result.withoutErr();
			assert option.isPresent();
			assert option.get() == 2;
		}
	}

	@Nested
	class And {
		final Result<Integer, String> input = ok(2);

		@Test
		void andOk() {
			var res = input.and(ok("hi"));
			assert ok("hi").equals(res);
		}

		@Test
		void andOkLazy() {
			var res = input.and(() -> ok("hi"));
			assert ok("hi").equals(res);
		}

		@Test
		void andErr() {
			var res = input.and(err("err"));
			assert err("err").equals(res);
		}

		@Test
		void andErrLazy() {
			var res = input.and(() -> err("err"));
			assert err("err").equals(res);
		}

		@Test
		@SuppressWarnings({"ConstantConditions"})
		void nonNull() {
			assertThrows(NullPointerException.class, () -> input.and((Result<Integer, String>) null));
			assertThrows(NullPointerException.class, () -> input.and((Supplier<Result<Integer, String>>) null));
			assertThrows(NullPointerException.class, () -> input.and((Supplier<Result<Integer, String>>) TestUtil::nullSupplier));
		}
	}

	@Nested
	class Or {
		final Result<Integer, String> input = ok(2);

		@Test
	    void orOk() {
			var res = input.or(ok(1));
			assert ok(2).equals(res);
		}

		@Test
		void orOkLazy() {
			var res = input.or(TestUtil::failIfCalled);
			assert ok(2).equals(res);
		}

		@Test
		void orErr() {
			var res = input.or(err("hi"));
			assert res.getOrThrow() == 2;
		}

		@Test
	    void orErrLazy() {
			var res = input.or(TestUtil::failIfCalled);
			assert res.getOrThrow() == 2;
		}
	}

	@Nested
	class Contains {
		@Test
	    void doesContainOk() {
		    assert ok(2).contains(2);
		    assert ok("hello").contains("hello");
		}

		@Test
		@SuppressWarnings("ConstantConditions")
	    void doesNotContainOk() {
		    assert !ok(2).contains(null);
		    assert !ok(2).contains(3);
		    assert !ok("hello").contains("bye");
		}

		@Test
		void containErr() {
			assert !ok(2).containsErr(null);
			assert !ok(2).containsErr(2);
		}
	}

	@Nested
	class Matches {
		@Test
		void okTrue() {
			assert ok(2).matches(ok -> ok == 2);
			assert ok("HELLO").matches("hello"::equalsIgnoreCase);
		}

		@Test
		void okFalse() {
			assert !ok(2).matches(ok -> ok == 3);
			assert !ok("HELLO").matches("hello"::equals);
		}

		@Test
		void errMatch() {
			assert !err(2).matches(err -> true);
			assert !Err.<Integer, Integer>of(2).matches(err -> err == 2);
			//noinspection ResultOfMethodCallIgnored
			err(2).matches(TestUtil::failIfCalled);
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			var input = ok("hello");
			assertThrows(NullPointerException.class, () -> input.matches(null));
			assertThrows(NullPointerException.class, () -> input.errMatches(null));
		}
	}

	@Nested
	class Unified {
		@Test
		void get() {
			TestData content = new TestData(1);
			var ok = ok(content);
			assert content == ok.getUnified();
		}
	}

	@Nested
	class Sequence {
		final Result<Integer, String> result = ok(2);

		@Test
		void iterator() {
			Iterator<Integer> iterator = result.iterator();
			assert iterator.hasNext();
			assert iterator.next() == 2;
			assert !iterator.hasNext();
		}

		@Test
		void forLoop() {
			int count = 0;
			for (int value : result) {
				assert value == 2;
				count++;
			}
			assert count == 1;
		}

		@Test
		void stream() {
			List<Integer> list = result.stream().collect(toList());
			assert list.size() == 1;
			assert list.get(0) == 2;
		}
	}

	@Nested
	class ObjectMethods {

		@Test
		@SuppressWarnings({"EqualsWithItself", "EqualsBetweenInconvertibleTypes", "ConstantConditions"})
		void testEquals() {
			var ok = ok("hello");
			assert ok.equals(ok);
			assert ok("hello").equals(ok("hello"));
			assert !ok("hello").equals(ok("bye"));
			assert !ok("hello").equals(ok(1));
			assert !ok("hello").equals(err("bye"));
			assert !ok("hello").equals(null);
		}

		@Test
		void testHashCode() {
			assert ok("hello").hashCode() == ok("hello").hashCode();
			assert ok("hello").hashCode() != ok("bye").hashCode();
			assert ok("hello").hashCode() != ok(1).hashCode();
			assert ok("hello").hashCode() != err("hello").hashCode();
		}

		@Test
		void testToString() {
			assert "Ok(1)".equals(ok(1).toString());
			assert "Ok(hello)".equals(ok("hello").toString());
		}
	}
}
