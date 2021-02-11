package nl.markv.result;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public final class Err<T, E> implements Result<T, E> {

	private final @Nonnull E value;

	public Err(@Nonnull E value) {
		requireNonNull(value, "cannot construct Err from a null value");
		this.value = value;
	}

	@Nonnull
	public static <T, E> Err<T, E> of(@Nonnull E value) {
		return new Err<>(value);
	}

	@Override
	public boolean isOk() {
		return false;
	}

	@Nonnull
	public E get() {
		return value;
	}

	@Nonnull
	@Override
	public T getOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier) {
		requireNonNull(exceptionSupplier);
		throw exceptionSupplier.get();
	}

	@Nonnull
	@Override
	public E getErrOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier) {
		requireNonNull(exceptionSupplier);
		return value;
	}

	@Nonnull
	@Override
	public Object getUnified() {
		return value;
	}
}
