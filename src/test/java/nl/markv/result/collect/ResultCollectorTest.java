package nl.markv.result.collect;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import nl.markv.result.Err;
import nl.markv.result.Ok;
import nl.markv.result.Result;

import static nl.markv.result.collect.ResultCollector.toList;
import static nl.markv.result.collect.ResultCollector.toMutableList;
import static nl.markv.result.collect.ResultCollector.toMutableSet;
import static nl.markv.result.collect.ResultCollector.toOrderedSet;
import static nl.markv.result.collect.ResultCollector.toSet;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResultCollectorTest {
	//TODO @mark: too many type annotations needed...

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

	@Nested
	class Sets {
		@Test
		void empty() {
			var resultSet = Stream.<Result<Integer, String>>of().collect(toSet());
			assert resultSet.isOk();
			assert resultSet.getOrThrow().isEmpty();
		}

		@Test
		void singleOk() {
			var resultSet = Stream.of(Ok.of(2)).collect(toSet());
			assert resultSet.isOk();
			var set = resultSet.getOrThrow();
			assert set.size() == 1;
			assert set.contains(2);
		}

		@Test
		void singleErr() {
			var resultSet = Stream.of(Err.of(2)).collect(toSet());
			assert resultSet.isErr();
			assert resultSet.getErrOrThrow() == 2;
		}

		@Test
		void multipleOk() {
			var resultSet = Stream.of(Ok.of(2), Ok.of(4), Ok.of(8)).collect(toSet());
			assert resultSet.isOk();
			var set = resultSet.getOrThrow();
			assert set.size() == 3;
			assert set.contains(2);
			assert set.contains(4);
			assert set.contains(8);
		}

		@Test
		void containsErr() {
			var resultSet = Stream.<Result<Integer, Integer>>of(Ok.of(2), Err.of(4), Ok.of(8)).collect(toSet());
			assert resultSet.isErr();
			assert resultSet.getErrOrThrow() == 4;
		}

		@Test
		void multipleErrs() {
			// Cannot know which error will be returned in this case
			var resultSet = Stream.of(Err.of(2), Err.of(4), Err.of(8)).collect(toSet());
			assert resultSet.isErr();
			assert Set.of(2, 4, 8).contains(resultSet.getErrOrThrow());
		}

		@Test
		void mutable() {
			var resultSet = Stream.of(Ok.of(2), Ok.of(4)).collect(toMutableSet());
			assert resultSet.matches(set -> set.size() == 2);
			resultSet.ifOk(set -> set.add(7));
			assert resultSet.matches(set -> set.size() == 3);
		}

		@Test
		void immutable() {
			var resultSet = Stream.of(Ok.of(2), Ok.of(4)).collect(toSet());
			assertThrows(RuntimeException.class, () -> resultSet.ifOk(list -> list.add(7)));
		}

		@Test
		void ordered() {
			var resultSet = Stream.of(Ok.of(2), Ok.of(4), Ok.of(8), Ok.of(16), Ok.of(32)).collect(toOrderedSet());
			var iter = resultSet.getOrThrow().iterator();
			assert iter.next() == 2;
			assert iter.next() == 4;
			assert iter.next() == 8;
			assert iter.next() == 16;
			assert iter.next() == 32;
		}
	}
}
