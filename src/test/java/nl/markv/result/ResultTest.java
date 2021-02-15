package nl.markv.result;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static nl.markv.result.Result.transpose;

class ResultTest {

	@Nested
	class Attempting {

		private double fail() throws Exception {
			throw new Exception("checked");
		}

		@Test
		void success() {
			Result<String, Exception> ok = Result.attempt(() -> "hello");
			assert "hello".equals(ok.getOrThrow());
		}

		@Test
		void successException() {
			Result<Exception, Exception> ok = Result.attempt(() -> new IllegalStateException("returned exception"));
			assert ok.getOrThrow() instanceof IllegalStateException;
			assert "returned exception".equals(ok.getOrThrow().getMessage());
		}

		@Test
		void uncheckedFailure() {
			Result<Integer, Exception> err = Result.attempt(() -> {
				throw new IllegalStateException("failed attempt");
			});
			assert err.getErrOrThrow() instanceof IllegalStateException;
			assert "failed attempt".equals(err.getErrOrThrow().getMessage());
		}

		@Test
		void checkedFailure() {
			Result<Double, Exception> err = Result.attempt(this::fail);
			assert Exception.class.equals(err.getErrOrThrow().getClass());
			assert "checked".equals(err.getErrOrThrow().getMessage());
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
		    var resultList = transpose(singletonList(Ok.of(2)));
		    assert resultList.isOk();
			var list = resultList.getOrThrow();
		    assert list.size() == 1;
		    assert list.get(0) == 2;
		}

		@Test
	    void singleErr() {
		    var resultList = transpose(singletonList(Err.of(2)));
		    assert resultList.isErr();
		    assert resultList.getErrOrThrow() == 2;
		}

		@Test
	    void multipleOk() {
		    var resultList = transpose(asList(Ok.of(2), Ok.of(4), Ok.of(8)));
		    assert resultList.isOk();
			var list = resultList.getOrThrow();
		    assert list.size() == 3;
		    assert list.get(0) == 2;
		    assert list.get(1) == 4;
		    assert list.get(2) == 8;
		}

		@Test
	    void errAtStart() {
			var resultList = transpose(asList(Err.of(2), Ok.of(4), Ok.of(8)));
		    assert resultList.isErr();
		    assert resultList.getErrOrThrow() == 2;
		}

		@Test
	    void errInMiddle() {
			var resultList = transpose(asList(Ok.of(2), Err.of(4), Ok.of(8)));
		    assert resultList.isErr();
		    assert resultList.getErrOrThrow() == 4;
		}

		@Test
	    void errAtEnd() {
			var resultList = transpose(asList(Ok.of(2), Ok.of(4), Err.of(8)));
		    assert resultList.isErr();
		    assert resultList.getErrOrThrow() == 8;
		}

		@Test
	    void multipleErrors() {
			var resultList = transpose(asList(Err.of(2), Err.of(4), Err.of(8)));
		    assert resultList.isErr();
		    assert resultList.getErrOrThrow() == 2;
		}
	}

	@Nested
	class TransposeSet {
		@Test
		void empty() {
			var resultList = transpose(emptySet());
			assert resultList.isOk();
			assert resultList.getOrThrow().isEmpty();
		}

		@Test
		void singleOk() {
			var resultList = transpose(singleton(Ok.of(2)));
			assert resultList.isOk();
			var list = resultList.getOrThrow();
			assert list.size() == 1;
			assert list.contains(2);
		}

		@Test
		void singleErr() {
			var resultList = transpose(singleton(Err.of(2)));
			assert resultList.isErr();
			assert resultList.getErrOrThrow() == 2;
		}

		@Test
		void multipleOk() {
			var resultList = transpose(Set.of(Ok.of(2), Ok.of(4), Ok.of(8)));
			assert resultList.isOk();
			var list = resultList.getOrThrow();
			assert list.size() == 3;
			assert list.contains(2);
			assert list.contains(4);
			assert list.contains(8);
		}

		@Test
		void containsErr() {
			var resultList = transpose(Set.of(Ok.of(2), Err.of(4), Ok.of(8)));
			assert resultList.isErr();
			assert resultList.getErrOrThrow() == 4;
		}

		@Test
		void multipleErrs() {
			// Cannot know which error will be returned in this case
			var resultList = transpose(Set.of(Err.of(2), Err.of(4), Err.of(8)));
			assert resultList.isErr();
			assert Set.of(2, 4, 8).contains(resultList.getErrOrThrow());
		}
	}
	
	@Nested
	class TransposeOptional {
		@Test
	    void SomeOk() {
			var result = transpose(Optional.of(Ok.of(2)));
			assert result.isOk();
			assert result.getOrThrow().isPresent();
			assert result.getOrThrow().get() == 2;
		}

		@Test
	    void SomeErr() {
			var result = transpose(Optional.of(Err.of(2)));
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
			var result = transpose(Ok.of(Optional.of(2)));
			assert result.isPresent();
			assert result.get().isOk();
			assert result.get().getOrThrow() == 2;
		}

		@Test
		void OkNone() {
			var result = transpose(Ok.of(Optional.empty()));
			assert result.isEmpty();
		}

		@Test
		void Err() {
			var result = transpose(Err.of(2));
			assert result.isPresent();
			assert result.get().isErr();
			assert result.get().getErrOrThrow() == 2;
		}
	}
}
