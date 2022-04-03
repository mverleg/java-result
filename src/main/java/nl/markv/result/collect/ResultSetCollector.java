package nl.markv.result.collect;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import javax.annotation.Nonnull;

import nl.markv.result.Err;
import nl.markv.result.Result;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

public class ResultSetCollector<T, E> implements Collector<Result<T, E>, ResultBuilder<Set<T>, E>, Result<Set<T>, E>> {

	private final boolean isOrdered;
	private final @Nonnull Function<ResultBuilder<Set<T>, E>, Result<Set<T>, E>> finisher;

	public ResultSetCollector(
			boolean isOrdered,
			@Nonnull Function<ResultBuilder<Set<T>, E>, Result<Set<T>, E>> finisher) {
		requireNonNull(finisher);
		this.isOrdered = isOrdered;
		this.finisher = finisher;
	}

	@Nonnull
	private ResultBuilder<Set<T>, E> supplierImpl() {
		return ResultBuilder.ok(isOrdered ? new LinkedHashSet<>() : new HashSet<>());
	}

	private void accumulatorImpl(@Nonnull ResultBuilder<Set<T>, E> currentList, @Nonnull Result<T, E> newResult) {
		if (currentList.isErr()) {
			return;
		}
		if (newResult instanceof Err<T, E> err) {
			currentList.toErr(err.get());
			return;
		}
		currentList.getOrThrow().add(newResult.getOrThrow());
	}

	@Nonnull
	private ResultBuilder<Set<T>, E> combinerImpl(@Nonnull ResultBuilder<Set<T>, E> oneList, @Nonnull ResultBuilder<Set<T>, E> otherList) {
		if (oneList.isErr()) {
			return oneList;
		}
		if (otherList.isErr()) {
			return otherList;
		}
		oneList.getOrThrow().addAll(otherList.getOrThrow());
		return oneList;
	}

	@Override
	@Nonnull
	public Supplier<ResultBuilder<Set<T>, E>> supplier() {
		return this::supplierImpl;
	}

	@Override
	@Nonnull
	public BiConsumer<ResultBuilder<Set<T>, E>, Result<T, E>> accumulator() {
		return this::accumulatorImpl;
	}

	@Override
	@Nonnull
	public BinaryOperator<ResultBuilder<Set<T>, E>> combiner() {
		return this::combinerImpl;
	}

	@Override
	@Nonnull
	public Function<ResultBuilder<Set<T>, E>, Result<Set<T>, E>> finisher() {
		return finisher;
	}

	@Override
	@Nonnull
	public Set<Characteristics> characteristics() {
		return isOrdered ? emptySet() : singleton(Characteristics.UNORDERED);
	}
}
