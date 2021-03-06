package nl.markv.result;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
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
			assert "hello".equals(res.getOrThrow("my error"));
			assert "hello".equals(res.getOrThrow(IllegalStateException::new));
		}

		@Test
		void getErr() {
			Result<String, String> res = Ok.of("hello");
			assertThrows(WrongResultVariantException.class, res::getErrOrThrow);
			var ex = assertThrows(WrongResultVariantException.class, () -> res.getErrOrThrow("my error"));
			assert "my error".equals(ex.getMessage());
			assertThrows(IllegalStateException.class, () -> res.getErrOrThrow(IllegalStateException::new));
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			Result<String, String> res = Ok.of("hello");
			assertThrows(NullPointerException.class, () -> res.getErrOrThrow((String)null));
			assertThrows(NullPointerException.class, () -> res.getErrOrThrow(() -> null));
		}
	}

	@Nested
	class Mapping {
		@Test
		void okMap() {
			Result<Integer, Integer> ok1 = Ok.of(2);
			Result<String, Integer> ok2 = ok1.map(nr -> String.valueOf(2 * nr));
			assert "4".equals(ok2.getOrThrow());
		}

		@Test
		void errMap() {
			Result<Integer, Integer> ok1 = Ok.of(2);
			Result<Integer, String> ok2 = ok1.mapErr(nr -> String.valueOf(2 * nr));
			assert ok2.getOrThrow() == 2;
		}

		@Test
		void okFlatMapOk() {
			Result<Integer, Integer> ok1 = Ok.of(2);
			Result<String, Integer> ok2 = ok1.flatMap(nr -> Ok.of(String.valueOf(2 * nr)));
			assert "4".equals(ok2.getOrThrow());
		}

		@Test
		void okFlatMapErr() {
			Result<Integer, String> ok = Ok.of(2);
			Result<Integer, String> err = ok.flatMap(nr -> Err.of(String.valueOf(2 * nr)));
			assert "4".equals(err.getErrOrThrow());
		}

		@Test
		void errFlatMapOk() {
			Result<Integer, Integer> ok1 = Ok.of(2);
			Result<Integer, String> ok2 = ok1.flatMapErr(nr -> Ok.of(2 * nr));
			assert ok2.getOrThrow() == 2;
		}

		@Test
		void errFlatMapErr() {
			Result<Integer, Integer> ok1 = Ok.of(2);
			Result<Integer, String> ok2 = ok1.flatMapErr(nr -> Err.of(String.valueOf(2 * nr)));
			assert ok2.getOrThrow() == 2;
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			Result<String, String> res = Ok.of("hello");
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
		    Ok.of(2).ifOk(value -> {
		    	assert value == 2;
				toggle.turnOn();
			});
		    assert toggle.isOn();
		}

		@Test
		void ifErr() {
			Ok.of(2).ifErr(TestUtil::failIfCalled);
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			Result<String, String> res = Ok.of("hello");
			assertThrows(NullPointerException.class, () -> res.ifOk(null));
			assertThrows(NullPointerException.class, () -> res.ifErr(null));
		}
	}
	
	@Nested
	class IfEither {
		@Test
		void ifEither() {
			var toggle = new TestUtil.Toggle();
			Ok.of(2).ifEither(
					ok -> toggle.turnOn(),
					TestUtil::failIfCalled
			);
			assert toggle.isOn();
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			Result<String, String> res = Ok.of("hello");
			assertThrows(NullPointerException.class, () -> res.ifEither(null, ignored -> {}));
			assertThrows(NullPointerException.class, () -> res.ifEither(ignored -> {}, null));
		}
	}

	@Nested
	class Branch {
		@Test
	    void toNumber() {
			int result = Ok.of(2).branch(
					ok -> ok * 3,
					TestUtil::failIfCalled
			);
			assert result == 6;
		}

		@Test
	    void toResult() {
			var result = Ok.of(2).branch(
					ok -> Err.of("wrong!"),
					TestUtil::failIfCalled
			);
			assert result.isErr();
			assert "wrong!".equals(result.getErrOrThrow());
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			Result<String, String> res = Ok.of("hello");
			assertThrows(NullPointerException.class, () -> res.branch(null, ignored -> 1));
			assertThrows(NullPointerException.class, () -> res.branch(ignored -> null, ignored -> 1));
			assertThrows(NullPointerException.class, () -> res.branch(ignored -> 1, null));
		}
	}

	@Nested
	class Solve {
		@Test
	    void solve() {
			var result = Ok.of(2).solve(TestUtil::failIfCalled);
			assert result == 2;
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
		Result<Integer, Double> result = Result.ok(1);

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
			assert result.okOrNullable(() -> null) == 1;
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
			assert result.errOrNullable(() -> null) == null;
		}

		@Test
		void errOrNull() {
			assert result.errOrNull() == null;
		}

		@Test
		@SuppressWarnings({"ConstantConditions"})
		void nonNull() {
			Result<String, String> input = Ok.of("hello");
			assertThrows(NullPointerException.class, () -> input.okOr((String)null));
			assertThrows(NullPointerException.class, () -> input.okOr((Supplier<String>) null));
			assertThrows(NullPointerException.class, () -> input.okOrNullable((Supplier<String>) null));
			assertThrows(NullPointerException.class, () -> input.errOr((String)null));
			assertThrows(NullPointerException.class, () -> input.errOr((Supplier<String>) null));
			assertThrows(NullPointerException.class, () -> input.errOr(() -> null));
			assertThrows(NullPointerException.class, () -> input.errOrNullable((Supplier<String>) null));
		}
	}

	@Nested
	class Adapt {
		@Test
		void changeOkType() {
			Result<Integer, Integer> ok = Ok.of(1);
			assertThrows(WrongResultVariantException.class, ok::adaptOk);
		}

		@Test
		void changeErrType() {
			Result<Integer, String> ok1 = Ok.of(1);
			Result<Integer, Integer> ok2 = ok1.adaptErr();
			assert ok2.getOrThrow() == 1;
		}
	}

	@Nested
	class Without {
		private final Result<Integer, String> result = Ok.of(2);

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

	//TODO @mark: change Ok.of(.) to just ok(.) everywhere
	//TODO @mark: use 'var' everywhere to prevent type inference refressions

	@Nested
	class And {
		private final Result<Integer, String> input = Ok.of(2);

		@Test
		void andOk() {
			var res = input.and(Ok.of("hi"));
			assert Ok.of("hi").equals(res);
		}

		@Test
		void andOkLazy() {
			var res = input.and(() -> Ok.of("hi"));
			assert Ok.of("hi").equals(res);
		}

		@Test
		void andErr() {
			var res = input.and(Err.of("err"));
			assert Err.of("err").equals(res);
		}

		@Test
		void andErrLazy() {
			var res = input.and(() -> Err.of("err"));
			assert Err.of("err").equals(res);
		}

		@Test
		@SuppressWarnings({"ConstantConditions"})
		void nonNull() {
			assertThrows(NullPointerException.class, () -> input.and((Result<Integer, String>) null));
			assertThrows(NullPointerException.class, () -> input.and((Supplier<Result<Integer, String>>) null));
			assertThrows(NullPointerException.class, () -> input.and((Supplier<Result<Integer, String>>) () -> null));
		}
	}

	@Nested
	class Or {
		private final Result<Integer, String> input = Ok.of(2);

		@Test
	    void orOk() {
			var res = input.or(Ok.of(1));
			assert Ok.of(2).equals(res);
		}

		@Test
		void orOkLazy() {
			var res = input.or(TestUtil::failIfCalled);
			assert Ok.of(2).equals(res);
		}

		@Test
		void orErr() {
			var res = input.or(Err.of("hi"));
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
		    assert Ok.of(2).contains(2);
		    assert Ok.of("hello").contains("hello");
		}

		@Test
		@SuppressWarnings("ConstantConditions")
	    void doesNotContainOk() {
		    assert !Ok.of(2).contains(null);
		    assert !Ok.of(2).contains(3);
		    assert !Ok.of("hello").contains("bye");
		}

		@Test
		void containErr() {
			assert !Ok.of(2).containsErr(null);
			assert !Ok.of(2).containsErr(2);
		}
	}

	@Nested
	class Matches {
		@Test
		void okTrue() {
			assert Ok.of(2).matches(ok -> ok == 2);
			assert Ok.of("HELLO").matches("hello"::equalsIgnoreCase);
		}

		@Test
		void okFalse() {
			assert !Ok.of(2).matches(ok -> ok == 3);
			assert !Ok.of("HELLO").matches("hello"::equals);
		}

		@Test
		void err() {
			assert !Err.of(2).matches(err -> true);
			assert !Err.<Integer, Integer>of(2).matches(err -> err == 2);
			//noinspection ResultOfMethodCallIgnored
			Err.of(2).matches(TestUtil::failIfCalled);
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			Result<String, String> input = Ok.of("hello");
			assertThrows(NullPointerException.class, () -> input.matches(null));
			assertThrows(NullPointerException.class, () -> input.errMatches(null));
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
	class Sequence {
		private final Result<Integer, String> result = Ok.of(2);

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
			assert Ok.of("hello").hashCode() != Err.of("hello").hashCode();
		}

		@Test
		void testToString() {
			assert "Ok(1)".equals(Ok.of(1).toString());
			assert "Ok(hello)".equals(Ok.of("hello").toString());
		}
	}
}
