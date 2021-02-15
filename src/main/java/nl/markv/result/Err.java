package nl.markv.result;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Collections.emptyIterator;
import static java.util.Objects.requireNonNull;
import static nl.markv.result.None.none;

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

	@Nonnull
	public static <T> Err<T, None> empty() {
		return Err.of(none);
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
		requireNonNull(converter);
		return adaptOk();
	}

	@Nonnull
	@Override
	public <F> Result<T, F> mapErr(@Nonnull Function<E, F> converter) {
		return Err.of(converter.apply(value));
	}

	@Override
	public void ifOk(@Nonnull Consumer<T> action) {
		requireNonNull(action);
	}

	@Override
	public void ifErr(@Nonnull Consumer<E> action) {
		action.accept(value);
	}

	@Override
	public void ifEither(@Nonnull Consumer<T> okAction, @Nonnull Consumer<E> errAction) {
		errAction.accept(value);
	}

	@Nonnull
	@Override
	public <R> R branch(@Nonnull Function<T, R> okConverter, @Nonnull Function<E, R> errHandler) {
		return requireNonNull(errHandler.apply(value));
	}

	@Nonnull
	@Override
	public T solve(@Nonnull Function<E, T> errToOkConverter) {
		return requireNonNull(errToOkConverter.apply(value));
	}

	@Nonnull
	@Override
	public T okOr(@Nonnull T alternative) {
		requireNonNull(alternative);
		return alternative;
	}

	@Nonnull
	@Override
	public T okOr(@Nonnull Supplier<T> alternativeSupplier) {
		return requireNonNull(alternativeSupplier.get());
	}

	@Nonnull
	@Override
	public E errOr(@Nonnull E alternative) {
		requireNonNull(alternative);
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

	@Nonnull
	@Override
	public <U> Result<U, E> and(@Nonnull Result<U, E> next) {
		// See note about casting in 'adaptOk'
		//noinspection unchecked
		return (Result<U, E>) this;
	}

	@Nonnull
	@Override
	public <U> Result<U, E> and(@Nonnull Supplier<Result<U, E>> nextSupplier) {
		// See note about casting in 'adaptOk'
		//noinspection unchecked
		return (Result<U, E>) this;
	}

	@Nonnull
	@Override
	public <F> Result<T, F> or(@Nonnull Result<T, F> next) {
		return requireNonNull(next);
	}

	@Nonnull
	@Override
	public <F> Result<T, F> or(@Nonnull Supplier<Result<T, F>> nextSupplier) {
		return requireNonNull(nextSupplier.get());
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

	@Override
	public boolean matches(@Nonnull Predicate<T> okPredicate) {
		requireNonNull(okPredicate);
		return false;
	}

	@Override
	public boolean errMatches(@Nonnull Predicate<E> errPredicate) {
		return errPredicate.test(value);
	}

	@Nonnull
	@Override
	public Object getUnified() {
		return value;
	}

	@Override
	public boolean equals(@Nullable Object other) {
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
