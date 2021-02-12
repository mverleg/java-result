package nl.markv.result;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import static java.util.Collections.singletonList;
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
	public <U> Result<U, E> map(@Nonnull Function<T, U> converter) {
		return Ok.of(converter.apply(value));
	}

	@Nonnull
	@Override
	public <F> Result<T, F> mapErr(@Nonnull Function<E, F> converter) {
		return adaptErr();
	}

	@Nonnull
	@Override
	public <U> Result<U, E> adaptOk() {
		throw new WrongResultVariantException("Attempted to call 'adaptOk' on a Result containing " + toString() +
				"; this only succeeds if the Result is Err. Use 'map' to convert the Ok value.");
	}

	@Nonnull
	@Override
	public <F> Result<T, F> adaptErr() {
		// This is implemented using a cast. It feels a bit dirty to cast something that it genuinely of
		// type Result<T, E> to a different type Result<T, F>, which is not a supertype. But generic types
		// are erased at runtime, so it works well, and it's more efficient than making a new object.

		//noinspection unchecked
		return (Result<T, F>) this;
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
	@Nonnull
	public String toString() {
		return "Ok(" + value + ")";
	}

	@Override
	@Nonnull
	public Iterator<T> iterator() {
		return singletonList(value).iterator();
	}

	@Override
	@Nonnull
	public Stream<T> stream() {
		return Stream.of(value);
	}
}
