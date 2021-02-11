package nl.markv.result;

import java.util.Objects;
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
		requireNonNull(exceptionSupplier);
		return value;
	}

	@Nonnull
	@Override
	public E getErrOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier) {
		requireNonNull(exceptionSupplier);
		throw exceptionSupplier.get();
	}

	@Nonnull
	@Override
	public Object getUnified() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof Ok<?, ?> otherOk)) {
			return false;
		}
		return value.equals(otherOk.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(1, value);
	}

	@Override
	public String toString() {
		return "Ok(" + value + ")";
	}
}
