package nl.markv.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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
	public <F> Result<T, F> adaptErr() {
		throw new WrongResultVariantException("Attempted to call 'adaptErr' on a Result containing " + toString() +
				"; this only succeeds if the Result is Ok.");
	}

	@Nonnull
	@Override
	public Object getUnified() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof Err<?, ?> otherErr)) {
			return false;
		}
		return value.equals(otherErr.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(2, value);
	}

	@Override
	@Nonnull
	public String toString() {
		return "Err(" + value + ")";
	}

	@Override
	@Nonnull
	public Iterator<T> iterator() {
		return emptyIterator();
	}

	@Nonnull
	public Stream<T> stream() {
		return Stream.of();
	}
}
