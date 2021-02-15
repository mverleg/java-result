package nl.markv.result.collect;

import javax.annotation.Nonnull;

import nl.markv.result.Err;
import nl.markv.result.Ok;
import nl.markv.result.Result;

public final class ResultBuilder<T, E> {

	@Nonnull
	private Result<T, E> current;

	private ResultBuilder(@Nonnull Result<T, E> current) {
		this.current = current;
	}

	@Nonnull
	static <T, E> ResultBuilder<T, E> from(@Nonnull Result<T, E> result) {
		return new ResultBuilder<>(result);
	}

	@Nonnull
	static <T, E> ResultBuilder<T, E> ok(@Nonnull T value) {
		return new ResultBuilder<>(Ok.of(value));
	}

	@Nonnull
	static <T, E> ResultBuilder<T, E> err(@Nonnull E value) {
		return new ResultBuilder<>(Err.of(value));
	}

	@Nonnull
	ResultBuilder<T, E> toOk(@Nonnull T value) {
		current = Ok.of(value);
		return this;
	}

	@Nonnull
	ResultBuilder<T, E> toErr(@Nonnull E value) {
		current = Err.of(value);
		return this;
	}

	@Nonnull
	public Result<T, E> current() {
		return current;
	}

	@Nonnull
	public Result<T, E> build() {
		return current;
	}
}
