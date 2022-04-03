package nl.markv.result.collect;

import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.annotation.Nonnull;

import nl.markv.result.Err;
import nl.markv.result.Ok;
import nl.markv.result.Result;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * Utility class with several methods to create {@link Result}-related collectors.
 */
public class ResultCollector {

	private ResultCollector() {}

	/**
	 * Collector to collect a stream of {@link Result}s to a list. If any of the items is unsuccessful,
	 * the collection is stopped (if serial) and the error is returned. Otherwise, all the stream's {@link Ok}
	 * items are collected to an <strong>unmodifiable</strong> list.
	 */
	@Nonnull
	public static <T, E> ResultListCollector<T, E> toList() {
		return new ResultListCollector<>(builder ->
				builder.build().branch(
						list -> Ok.of(unmodifiableList(list)),
						Err::of));
	}

	/**
	 * Returns the first failure or a list of all okay values. Like {@link #toList()}, but the list is mutable.
	 */
	@Nonnull
	public static <T, E> ResultListCollector<T, E> toMutableList() {
		return new ResultListCollector<>(ResultBuilder::build);
	}

	/**
	 * Collector to collect a stream of {@link Result}s to a set. If any of the items is unsuccessful,
	 * the collection is stopped (if serial) and the error is returned. Otherwise, all the stream's {@link Ok}
	 * items are collected to an <strong>unmodifiable</strong>, unordered set.
	 */
	@Nonnull
	public static <T, E> ResultSetCollector<T, E> toSet() {
		return new ResultSetCollector<>(
				HashSet::new,
				builder -> builder.build().branch(
						set -> Ok.of(unmodifiableSet(set)),
						Err::of));
	}

	/**
	 * Returns any failure or a list of all okay values. Like {@link #toSet()}, but uses a mutable set.
	 */
	@Nonnull
	public static <T, E> ResultSetCollector<T, E> toMutableSet() {
		return new ResultSetCollector<>(HashSet::new, ResultBuilder::build);
	}

	/**
	 * Returns any failure or a list of all okay values. Like {@link #toSet()}, but uses an ordered set.
	 */
	@Nonnull
	public static <T, E> ResultSetCollector<T, E> toOrderedSet() {
		return new ResultSetCollector<>(
				LinkedHashSet::new,
				builder -> builder.build().branch(
						set -> Ok.of(unmodifiableSet(set)),
						Err::of));
	}
}
