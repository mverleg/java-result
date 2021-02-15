package nl.markv.result.collect;

import java.util.ArrayList;
import java.util.List;
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

public class ResultListCollector<T, E> implements Collector<Result<T, E>, ResultBuilder<List<T>, E>, Result<List<T>, E>> {
	//TODO @mark: tests

	@Nonnull
	private final Function<ResultBuilder<List<T>, E>, Result<List<T>, E>> finisher;

	public ResultListCollector(@Nonnull Function<ResultBuilder<List<T>, E>, Result<List<T>, E>> finisher) {
		requireNonNull(finisher);
		this.finisher = finisher;
	}

	@Nonnull
	private ResultBuilder<List<T>, E> supplierImpl() {
		return ResultBuilder.ok(new ArrayList<>());
	}

	private void accumulatorImpl(@Nonnull ResultBuilder<List<T>, E> currentList, @Nonnull Result<T, E> newResult) {
		if (currentList.isErr()) {
			return;
		}
		if (newResult instanceof Err<T, E> err) {
			currentList.toErr(err.get());
		}
		currentList.getOrThrow().add(newResult.getOrThrow());
	}

	private ResultBuilder<List<T>, E> combinerImpl(@Nonnull ResultBuilder<List<T>, E> oneList, @Nonnull ResultBuilder<List<T>, E> otherList) {
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
	public Supplier<ResultBuilder<List<T>, E>> supplier() {
		return this::supplierImpl;
	}

	@Override
	public BiConsumer<ResultBuilder<List<T>, E>, Result<T, E>> accumulator() {
		return this::accumulatorImpl;
	}

	@Override
	public BinaryOperator<ResultBuilder<List<T>, E>> combiner() {
		return this::combinerImpl;
	}

	@Override
	public Function<ResultBuilder<List<T>, E>, Result<List<T>, E>> finisher() {
		return finisher;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return emptySet();
	}
}
