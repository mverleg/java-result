package nl.markv.result.collect;

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
import static java.util.Objects.requireNonNull;

public class ResultSetCollector<T, E> implements Collector<Result<T, E>, ResultBuilder<Set<T>, E>, Result<Set<T>, E>> {
	//TODO @mark: tests

	private final @Nonnull Supplier<Set<T>> setCreator;
	private final @Nonnull Function<ResultBuilder<Set<T>, E>, Result<Set<T>, E>> finisher;

	public ResultSetCollector(
			@Nonnull Supplier<Set<T>> setCreator,
			@Nonnull Function<ResultBuilder<Set<T>, E>, Result<Set<T>, E>> finisher) {
		requireNonNull(setCreator);
		requireNonNull(finisher);
		this.setCreator = setCreator;
		this.finisher = finisher;
	}

	@Nonnull
	private ResultBuilder<Set<T>, E> supplierImpl() {
		return ResultBuilder.ok(setCreator.get());
	}

	private void accumulatorImpl(@Nonnull ResultBuilder<Set<T>, E> currentList, @Nonnull Result<T, E> newResult) {
		if (currentList.isErr()) {
			return;
		}
		if (newResult instanceof Err<T, E> err) {
			currentList.toErr(err.get());
		}
		currentList.getOrThrow().add(newResult.getOrThrow());
	}

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
	public Supplier<ResultBuilder<Set<T>, E>> supplier() {
		return this::supplierImpl;
	}

	@Override
	public BiConsumer<ResultBuilder<Set<T>, E>, Result<T, E>> accumulator() {
		return this::accumulatorImpl;
	}

	@Override
	public BinaryOperator<ResultBuilder<Set<T>, E>> combiner() {
		return this::combinerImpl;
	}

	@Override
	public Function<ResultBuilder<Set<T>, E>, Result<Set<T>, E>> finisher() {
		return finisher;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return emptySet();
	}
}
