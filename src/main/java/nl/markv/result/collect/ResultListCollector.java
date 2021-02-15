package nl.markv.result.collect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import nl.markv.result.Err;
import nl.markv.result.Result;

import static java.util.Collections.emptySet;

public class ResultListCollector<T, E> implements Collector<Result<T, E>, ResultBuilder<List<T>, E>, Result<List<T>, E>> {
	//TODO @mark: tests

	@Override
	public Supplier<ResultBuilder<List<T>, E>> supplier() {
		return () -> ResultBuilder.ok(new ArrayList<>());
	}

	@Override
	public BiConsumer<ResultBuilder<List<T>, E>, Result<T, E>> accumulator() {
		return (currentList, newResult) -> {
			if (currentList.isErr()) {
				return;
			}
			if (newResult instanceof Err<T, E> err) {
				currentList.toErr(err.get());
			}
			currentList.getOrThrow().add(newResult.getOrThrow());
		};
	}

	@Override
	public BinaryOperator<ResultBuilder<List<T>, E>> combiner() {
		return (oneList, otherList) -> {
			if (oneList.isErr()) {
				return oneList;
			}
			if (otherList.isErr()) {
				return otherList;
			}
			oneList.getOrThrow().addAll(otherList.getOrThrow());
			return oneList;
		};
	}

	@Override
	public Function<ResultBuilder<List<T>, E>, Result<List<T>, E>> finisher() {
		return ResultBuilder::build;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return emptySet();
	}
}
