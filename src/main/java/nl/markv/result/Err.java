package nl.markv.result;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Collections.emptyIterator;
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
	public <U> Result<U, E> map(@Nonnull Function<T, U> converter) {
		return adaptOk();
	}

	@Nonnull
	@Override
	public <F> Result<T, F> mapErr(@Nonnull Function<E, F> converter) {
		return Err.of(converter.apply(value));
	}

	@Nonnull
	@Override
	public T okOr(@Nonnull T alternative) {
		return alternative;
	}

	@Nonnull
	@Override
	public T okOr(@Nonnull Supplier<T> alternativeSupplier) {
		return alternativeSupplier.get();
	}

	@Nonnull
	@Override
	public E errOr(@Nonnull E alternative) {
		return value;
	}

	@Nonnull
	@Override
	public E errOr(@Nonnull Supplier<E> alternativeSupplier) {
		return value;
	}

	@Nonnull
	@Override
	public <U> Result<U, E> adaptOk() {
		// This is implemented using a cast. It feels a bit dirty to cast something that it genuinely of
		// type Result<T, E> to a different type Result<U, E>, which is not a supertype. But generic types
		// are erased at runtime, so it works well, and it's more efficient than making a new object.

		//noinspection unchecked
		return (Result<U, E>) this;
	}

	@Nonnull
	@Override
	public <F> Result<T, F> adaptErr() {
		throw new WrongResultVariantException("Attempted to call 'adaptErr' on a Result containing " + toString() +
				"; this only succeeds if the Result is Ok. Use 'mapErr' to convert the error value.");
	}

	@Nonnull
	@Override
	public Optional<T> withoutErr() {
		return Optional.empty();
	}

	@Nonnull
	@Override
	public Optional<E> withoutOk() {
		return Optional.of(value);
	}

	@Override
	public boolean contains(@Nullable T ok) {
		return false;
	}

	@Override
	public boolean containsErr(@Nullable E err) {
		if (err == null) {
			return false;
		}
		return value.equals(err);
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
