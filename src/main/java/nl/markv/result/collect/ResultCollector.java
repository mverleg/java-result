package nl.markv.result.collect;

import javax.annotation.Nonnull;

import nl.markv.result.Err;
import nl.markv.result.Ok;

import static java.util.Collections.unmodifiableList;

public class ResultCollector {

	@Nonnull
	static <T, E> ResultListCollector<T, E> toMutableList() {
		return new ResultListCollector<>(ResultBuilder::build);
	}

	@Nonnull
	static <T, E> ResultListCollector<T, E> toList() {
		return new ResultListCollector<>(builder ->
				builder.build().branch(
						list -> Ok.of(unmodifiableList(list)),
						Err::of));
	}

//	@Nonnull
//	static <T, E> ResultListCollector<T, E> toSet() {
//		return new ResultListCollector<>(finisher);
//	}
//
//	@Nonnull
//	static <T, E> ResultListCollector<T, E> toUnmodifiableSet() {
//		return new ResultListCollector<>(finisher);
//	}
}
