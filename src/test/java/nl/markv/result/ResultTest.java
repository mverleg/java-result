package nl.markv.result;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static nl.markv.result.Result.err;
import static nl.markv.result.Result.flatten;
import static nl.markv.result.Result.ok;
import static nl.markv.result.Result.transpose;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResultTest {

	@Nested
	class FromOptional {
		@Test
	    void full() {
		    var result = Result.from(Optional.of("hello"));
		    assert result.isOk();
		    assert "hello".equals(result.getOrThrow());
		}

		@Test
		@SuppressWarnings({"OptionalAssignedToNull", "ConstantConditions"})
		void nonNull() {
			assertThrows(NullPointerException.class, () -> Result.from(null));
		}

		@Test
	    void empty() {
		    var result = Result.from(Optional.empty());
		    assert result.isErr();
		    assert result.getErrOrThrow() == None.get();
		}
	}

	@Nested
	class FromNullable {
		@Test
	    void nonNull() {
		    var result = Result.fromNullable("hello");
		    assert result.isOk();
		    assert "hello".equals(result.getOrThrow());
		}

		@Test
	    void isNull() {
		    var result = Result.fromNullable(null);
		    assert result.isErr();
		    assert result.getErrOrThrow() == None.get();
		}
	}

	@Nested
	class Attempting {

		private double fail() throws Exception {
			throw new Exception("checked");
		}

		@Test
		void success() {
			var ok = Result.attempt(() -> "hello");
			assert "hello".equals(ok.getOrThrow());
		}

		@Test
		void successException() {
			var ok = Result.attempt(() -> new IllegalStateException("returned exception"));
			assert ok.getOrThrow() instanceof IllegalStateException;
			assert "returned exception".equals(ok.getOrThrow().getMessage());
		}

		@Test
		void uncheckedFailure() {
			var err = Result.attempt(() -> {
				throw new IllegalStateException("failed attempt");
			});
			assert err.getErrOrThrow() instanceof IllegalStateException;
			assert "failed attempt".equals(err.getErrOrThrow().getMessage());
		}

		@Test
		void checkedFailure() {
			var err = Result.attempt(this::fail);
			assert Exception.class.equals(err.getErrOrThrow().getClass());
			assert "checked".equals(err.getErrOrThrow().getMessage());
		}

		@Test
		@SuppressWarnings("ConstantConditions")
		void nonNull() {
			assertThrows(NullPointerException.class, () -> Result.attempt(null));
			assertThrows(NullPointerException.class, () -> Result.attempt(() -> null));
		}
	}

	@Nested
	class TransposeList {
		@Test
	    void empty() {
		    var resultList = transpose(emptyList());
		    assert resultList.isOk();
		    assert resultList.getOrThrow().isEmpty();
		}

		@Test
	    void singleOk() {
		    var resultList = transpose(singletonList(ok(2)));
		    assert resultList.isOk();
			var list = resultList.getOrThrow();
		    assert list.size() == 1;
		    assert list.get(0) == 2;
		}

		@Test
	    void singleErr() {
		    var resultList = transpose(singletonList(err(2)));
		    assert resultList.isErr();
		    assert resultList.getErrOrThrow() == 2;
		}

		@Test
	    void multipleOk() {
		    var resultList = transpose(asList(ok(2), ok(4), ok(8)));
		    assert resultList.isOk();
			var list = resultList.getOrThrow();
		    assert list.size() == 3;
		    assert list.get(0) == 2;
		    assert list.get(1) == 4;
		    assert list.get(2) == 8;
		}

		@Test
	    void errAtStart() {
			var resultList = transpose(asList(err(2), ok(4), ok(8)));
		    assert resultList.isErr();
		    assert resultList.getErrOrThrow() == 2;
		}

		@Test
	    void errInMiddle() {
			var resultList = transpose(asList(ok(2), err(4), ok(8)));
		    assert resultList.isErr();
		    assert resultList.getErrOrThrow() == 4;
		}

		@Test
	    void errAtEnd() {
			var resultList = transpose(asList(ok(2), ok(4), err(8)));
		    assert resultList.isErr();
		    assert resultList.getErrOrThrow() == 8;
		}

		@Test
	    void multipleErrors() {
			var resultList = transpose(asList(err(2), err(4), err(8)));
		    assert resultList.isErr();
		    assert resultList.getErrOrThrow() == 2;
		}

		@Test
		@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
		void nonNull() {
			assertThrows(NullPointerException.class, () -> Result.transpose((List<Result<Integer, Integer>>) null));
		}
	}

	@Nested
	class TransposeSet {
		@Test
		void empty() {
			var resultSet = transpose(emptySet());
			assert resultSet.isOk();
			assert resultSet.getOrThrow().isEmpty();
		}

		@Test
		void singleOk() {
			var resultSet = transpose(singleton(ok(2)));
			assert resultSet.isOk();
			var set = resultSet.getOrThrow();
			assert set.size() == 1;
			assert set.contains(2);
		}

		@Test
		void singleErr() {
			var resultSet = transpose(singleton(err(2)));
			assert resultSet.isErr();
			assert resultSet.getErrOrThrow() == 2;
		}

		@Test
		void multipleOk() {
			var resultSet = transpose(Set.of(ok(2), ok(4), ok(8)));
			assert resultSet.isOk();
			var set = resultSet.getOrThrow();
			assert set.size() == 3;
			assert set.contains(2);
			assert set.contains(4);
			assert set.contains(8);
		}

		@Test
		void containsErr() {
			var resultSet = transpose(Set.of(ok(2), err(4), ok(8)));
			assert resultSet.isErr();
			assert resultSet.getErrOrThrow() == 4;
		}

		@Test
		void multipleErrs() {
			// Cannot know which error will be returned in this case
			var resultSet = transpose(Set.of(err(2), err(4), err(8)));
			assert resultSet.isErr();
			assert Set.of(2, 4, 8).contains(resultSet.getErrOrThrow());
		}

		@Test
		@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
		void nonNull() {
			assertThrows(NullPointerException.class, () -> Result.transpose((Set<Result<Integer, Integer>>) null));
		}
	}
	
	@Nested
	class TransposeOptional {
		@Test
	    void SomeOk() {
			var result = transpose(Optional.of(ok(2)));
			assert result.isOk();
			assert result.getOrThrow().isPresent();
			assert result.getOrThrow().get() == 2;
		}

		@Test
	    void SomeErr() {
			var result = transpose(Optional.of(err(2)));
			assert result.isErr();
			assert result.getErrOrThrow() == 2;
		}

		@Test
	    void None() {
			var result = transpose(Optional.empty());
			assert result.isOk();
			assert result.getOrThrow().isEmpty();
		}

		@Test
		void OkSome() {
			var result = transpose(ok(Optional.of(2)));
			assert result.isPresent();
			assert result.get().isOk();
			assert result.get().getOrThrow() == 2;
		}

		@Test
		void OkNone() {
			var result = transpose(ok(Optional.empty()));
			assert result.isEmpty();
		}

		@Test
		void Err() {
			var result = transpose(err(2));
			assert result.isPresent();
			assert result.get().isErr();
			assert result.get().getErrOrThrow() == 2;
		}

		@Test
		@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions", "OptionalAssignedToNull"})
		void nonNull() {
			assertThrows(NullPointerException.class, () -> Result.transpose((Optional<Result<Integer, Integer>>) null));
			assertThrows(NullPointerException.class, () -> Result.transpose((Result<Optional<Integer>, Integer>) null));
		}
	}

	@Nested
	class Flatten {
		@Test
	    void OkOk() {
			var result = flatten(ok(ok(2)));
		    assert result.isOk();
		    assert result.getOrThrow() == 2;
		}

		@Test
	    void OkErr() {
			var result = flatten(ok(err(2)));
		    assert result.isErr();
		    assert result.getErrOrThrow() == 2;
		}

		@Test
		void Err() {
			var result = flatten(err(2));
			assert result.isErr();
			assert result.getErrOrThrow() == 2;
		}

		@Test
		@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
		void nonNull() {
			assertThrows(NullPointerException.class, () -> Result.flatten(null));
		}
	}
}
