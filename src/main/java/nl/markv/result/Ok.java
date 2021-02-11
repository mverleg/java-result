package nl.markv.result;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public final class Ok<T, E> implements Result<T, E> {

	private final @Nonnull T value;

	public Ok(@Nonnull T value) {
		requireNonNull(value, "cannot construct Ok from a null value");
		this.value = value;
	}

	@Nonnull
	public static <T, E> Ok<T, E> of(@Nonnull T value) {
		return new Ok<>(value);
	}

	@Override
	public boolean isOk() {
		return true;
	}

	@Nonnull
	public T get() {
		return value;
	}

	@Nonnull
	@Override
	public T getOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier) {
		return value;
	}

	@Nonnull
	@Override
	public E getErrOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier) {
		throw exceptionSupplier.get();
	}

	@Nonnull
	@Override
	public Object getUnified() {
		return value;
	}
}
