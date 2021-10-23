package nl.markv.result.collect;

import javax.annotation.Nonnull;

import nl.markv.result.Err;
import nl.markv.result.Ok;
import nl.markv.result.Result;

final class ResultBuilder<T, E> {

	@Nonnull
	private Result<T, E> current;

	private ResultBuilder(@Nonnull Result<T, E> current) {
		this.current = current;
	}

	@Nonnull
	public static <T, E> ResultBuilder<T, E> ok(@Nonnull T value) {
		return new ResultBuilder<>(Ok.of(value));
	}

	@Nonnull
	public ResultBuilder<T, E> toErr(@Nonnull E value) {
		current = Err.of(value);
		return this;
	}

	public boolean isErr() {
		return current.isErr();
	}

	@Nonnull
	public T getOrThrow() {
		return current.getOrThrow();
	}

	@Nonnull
	public Result<T, E> build() {
		return current;
	}
}
