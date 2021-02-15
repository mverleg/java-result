package nl.markv.result.collect;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import nl.markv.result.Err;
import nl.markv.result.Ok;
import nl.markv.result.Result;

import static nl.markv.result.collect.ResultCollector.toList;
import static nl.markv.result.collect.ResultCollector.toMutableList;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResultCollectorTest {

	@Nested
	class Lists {
		@Test
		void empty() {
			var resultList = Stream.<Result<Integer, String>>of().collect(toList());
			assert resultList.isOk();
			assert resultList.getOrThrow().isEmpty();
		}

		@Test
		void singleOk() {
			var resultList = Stream.of(Ok.of(2)).collect(toList());
			assert resultList.isOk();
			var list = resultList.getOrThrow();
			assert list.size() == 1;
			assert list.get(0) == 2;
		}

		@Test
		void singleErr() {
			var resultList = Stream.of(Err.of(2)).collect(toList());
			assert resultList.isErr();
			assert resultList.getErrOrThrow() == 2;
		}

		@Test
		void multipleOk() {
			var resultList = Stream.of(Ok.of(2), Ok.of(4), Ok.of(8)).collect(toList());
			assert resultList.isOk();
			var list = resultList.getOrThrow();
			assert list.size() == 3;
			assert list.get(0) == 2;
			assert list.get(1) == 4;
			assert list.get(2) == 8;
		}

		@Test
		void errAtStart() {
			var resultList = Stream.<Result<Integer, Integer>>of(Err.of(2), Ok.of(4), Ok.of(8)).collect(toList());
			assert resultList.isErr();
			assert resultList.getErrOrThrow() == 2;
		}

		@Test
		void errInMiddle() {
			var resultList = Stream.<Result<Integer, Integer>>of(Ok.of(2), Err.of(4), Ok.of(8)).collect(toList());
			assert resultList.isErr();
			assert resultList.getErrOrThrow() == 4;
		}

		@Test
		void errAtEnd() {
			var resultList = Stream.<Result<Integer, Integer>>of(Ok.of(2), Ok.of(4), Err.of(8)).collect(toList());
			assert resultList.isErr();
			assert resultList.getErrOrThrow() == 8;
		}

		@Test
		void multipleErrors() {
			var resultList = Stream.of(Err.of(2), Err.of(4), Err.of(8)).collect(toList());
			assert resultList.isErr();
			assert resultList.getErrOrThrow() == 2;
		}

		@Test
		void mutable() {
			var resultList = Stream.of(Ok.of(2), Ok.of(4)).collect(toMutableList());
			assert resultList.matches(list -> list.size() == 2);
			resultList.ifOk(list -> list.add(7));
			assert resultList.matches(list -> list.size() == 3);
		}

		@Test
		void immutable() {
			var resultList = Stream.of(Ok.of(2), Ok.of(4)).collect(toList());
			assertThrows(RuntimeException.class, () -> resultList.ifOk(list -> list.add(7)));
		}
	}
}
