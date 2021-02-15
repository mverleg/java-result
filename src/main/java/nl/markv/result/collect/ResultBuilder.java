package nl.markv.result.collect;

import java.util.function.Consumer;
import java.util.function.Function;

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
	public static <T, E> ResultBuilder<T, E> from(@Nonnull Result<T, E> result) {
		return new ResultBuilder<>(result);
	}

	@Nonnull
	public static <T, E> ResultBuilder<T, E> ok(@Nonnull T value) {
		return new ResultBuilder<>(Ok.of(value));
	}

	@Nonnull
	public static <T, E> ResultBuilder<T, E> err(@Nonnull E value) {
		return new ResultBuilder<>(Err.of(value));
	}

	@Nonnull
	public ResultBuilder<T, E> toOk(@Nonnull T value) {
		current = Ok.of(value);
		return this;
	}

	@Nonnull
	public ResultBuilder<T, E> toErr(@Nonnull E value) {
		current = Err.of(value);
		return this;
	}

	public boolean isOk() {
		return current.isOk();
	}

	public boolean isErr() {
		return current.isErr();
	}

	public void ifOk(@Nonnull Consumer<T> action) {
		current.ifOk(action);
	}

	public void ifErr(@Nonnull Consumer<E> action) {
		current.ifErr(action);
	}

	public void ifEither(@Nonnull Consumer<T> okAction, Consumer<E> errAction) {
		current.ifEither(okAction, errAction);
	}

	@Nonnull
	public <R> R branch(@Nonnull Function<T, R> okConverter, Function<E, R> errHandler) {
		return current.branch(okConverter, errHandler);
	}

	@Nonnull
	public T getOrThrow() {
		return current.getOrThrow();
	}

	@Nonnull
	public E getErrOrThrow() {
		return current.getErrOrThrow();
	}

	@Nonnull
	public Result<T, E> build() {
		return current;
	}
}
