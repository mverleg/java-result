package nl.markv.result.collect;

import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.annotation.Nonnull;

import nl.markv.result.Err;
import nl.markv.result.Ok;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

public class ResultCollector {
	//TODO @mark: javadocs

	@Nonnull
	static <T, E> ResultListCollector<T, E> toList() {
		return new ResultListCollector<>(builder ->
				builder.build().branch(
						list -> Ok.of(unmodifiableList(list)),
						Err::of));
	}

	@Nonnull
	static <T, E> ResultListCollector<T, E> toMutableList() {
		return new ResultListCollector<>(ResultBuilder::build);
	}

	@Nonnull
	static <T, E> ResultSetCollector<T, E> toSet() {
		return new ResultSetCollector<>(
				LinkedHashSet::new,
				builder -> builder.build().branch(
						set -> Ok.of(unmodifiableSet(set)),
						Err::of));
	}

	@Nonnull
	static <T, E> ResultSetCollector<T, E> toMutableSet() {
		return new ResultSetCollector<>(HashSet::new, ResultBuilder::build);
	}

	@Nonnull
	static <T, E> ResultSetCollector<T, E> toOrderedSet() {
		return new ResultSetCollector<>(
				LinkedHashSet::new,
				builder -> builder.build().branch(
						set -> Ok.of(unmodifiableSet(set)),
						Err::of));
	}
}
