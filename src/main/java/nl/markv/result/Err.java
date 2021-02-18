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

	/**
	 * @see #of(E)
	 */
	Err(@Nonnull E value) {
		requireNonNull(value, "cannot construct Err from a null value");
		this.value = value;
	}

	/**
	 * Create a new, unsuccessful {@link Result}. Same as {@link Result#err(E)}.
	 */
	@Nonnull
	public static <T, E> Err<T, E> of(@Nonnull E value) {
		return new Err<>(value);
	}

	/**
	 * Create a failed {@link Result} where the {@link Err} type is {@link None}.
	 *
	 * This is very similar to {@link Optional}, which may be preferable.
	 */
	@Nonnull
	public static <T> Err<T, None> empty() {
		return Err.of(none);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOk() {
		return false;
	}

	/**
	 * Returns the value contained by this {@link Ok}. Since this can only be called ok {@link Ok} and not on
	 * {@link Result}, this is safe - it will always return a non-{@code null} result without throwing.
	 */
	@Nonnull
	public E get() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Marked as deprecated because calling {@link #getOrThrow()} on {@link Err} always fails.
	 * 	It is not deprecated to call this on {@link Result}, but on {@link Err} use {@link #get()}.
	 */
	@Deprecated
	@Nonnull
	@Override
	public T getOrThrow() {
		return getOrThrow("Attempted to get Ok from Result, but content is " + value);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Marked as deprecated because calling {@link #getOrThrow(String)} on {@link Err} always fails.
	 * 	It is not deprecated to call this on {@link Result}, but on {@link Err} use {@link #get()}.
	 */
	@Deprecated
	@Nonnull
	@Override
	public T getOrThrow(@Nonnull String exceptionMessage) {
		return getOrThrow(() -> new WrongResultVariantException(exceptionMessage));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Marked as deprecated because calling {@link #getOrThrow(Supplier)} on {@link Err} always fails.
	 * 	It is not deprecated to call this on {@link Result}, but on {@link Err} use {@link #get()}.
	 */
	@Deprecated
	@Nonnull
	@Override
	public T getOrThrow(@Nonnull Supplier<RuntimeException> exceptionSupplier) {
		requireNonNull(exceptionSupplier);
		throw exceptionSupplier.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public E getErrOrThrow() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public E getErrOrThrow(@Nonnull String exceptionMessage) {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public E getErrOrThrow(@Nonnull Supplier<RuntimeException> exceptionSupplier) {
		requireNonNull(exceptionSupplier);
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <U> Result<U, E> map(@Nonnull Function<T, U> converter) {
		requireNonNull(converter);
		return adaptOk();
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <U> Result<U, E> flatMap(@Nonnull Function<T, Result<U, E>> converter) {
		return adaptOk();
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <F> Result<T, F> mapErr(@Nonnull Function<E, F> converter) {
		return Err.of(converter.apply(value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <F> Result<T, F> flatMapErr(@Nonnull Function<E, Result<T, F>> converter) {
		return requireNonNull(converter.apply(value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ifOk(@Nonnull Consumer<T> action) {
		requireNonNull(action);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ifErr(@Nonnull Consumer<E> action) {
		action.accept(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ifEither(@Nonnull Consumer<T> okAction, @Nonnull Consumer<E> errAction) {
		errAction.accept(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <R> R branch(@Nonnull Function<T, R> okConverter, @Nonnull Function<E, R> errHandler) {
		return requireNonNull(errHandler.apply(value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public T solve(@Nonnull Function<E, T> errToOkConverter) {
		return requireNonNull(errToOkConverter.apply(value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public T okOr(@Nonnull T alternative) {
		requireNonNull(alternative);
		return alternative;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public T okOr(@Nonnull Supplier<T> alternativeSupplier) {
		return requireNonNull(alternativeSupplier.get());
	}

	/**
	 * {@inheritDoc}
	 */
	//TODO @mark: test
	@Nullable
	@Override
	public T okOrNullable(@Nullable T alternative) {
		return alternative;
	}

	/**
	 * {@inheritDoc}
	 */
	//TODO @mark: test
	@Nullable
	@Override
	public T okOrNullable(@Nonnull Supplier<T> alternativeSupplier) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	//TODO @mark: test
	@Nullable
	@Override
	public T okOrNull() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public E errOr(@Nonnull E alternative) {
		requireNonNull(alternative);
		return requireNonNull(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public E errOr(@Nonnull Supplier<E> alternativeSupplier) {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	//TODO @mark: test
	@Nonnull
	@Override
	public E errOrNullable(@Nullable E alternative) {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	//TODO @mark: test
	@Nullable
	@Override
	public E errOrNullable(@Nonnull Supplier<E> alternativeSupplier) {
		return alternativeSupplier.get();
	}

	/**
	 * {@inheritDoc}
	 */
	//TODO @mark: test
	@Nonnull
	@Override
	public E errOrNull() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <U> Result<U, E> adaptOk() {
		// This is implemented using a cast. It feels a bit dirty to cast something that it genuinely of
		// type Result<T, E> to a different type Result<U, E>, which is not a supertype. But generic types
		// are erased at runtime, so it works well, and it's more efficient than making a new object.

		//noinspection unchecked
		return (Result<U, E>) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <F> Result<T, F> adaptErr() {
		throw new WrongResultVariantException("Attempted to call 'adaptErr' on a Result containing " + toString() +
				"; this only succeeds if the Result is Ok. Use 'mapErr' to convert the error value.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public Optional<T> withoutErr() {
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public Optional<E> withoutOk() {
		return Optional.of(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <U> Result<U, E> and(@Nonnull Result<U, E> next) {
		// See note about casting in 'adaptOk'
		//noinspection unchecked
		return (Result<U, E>) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <U> Result<U, E> and(@Nonnull Supplier<Result<U, E>> nextSupplier) {
		// See note about casting in 'adaptOk'
		//noinspection unchecked
		return (Result<U, E>) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <F> Result<T, F> or(@Nonnull Result<T, F> next) {
		return requireNonNull(next);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <F> Result<T, F> or(@Nonnull Supplier<Result<T, F>> nextSupplier) {
		return requireNonNull(nextSupplier.get());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(@Nullable T ok) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsErr(@Nullable E err) {
		if (err == null) {
			return false;
		}
		return value.equals(err);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(@Nonnull Predicate<T> okPredicate) {
		requireNonNull(okPredicate);
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean errMatches(@Nonnull Predicate<E> errPredicate) {
		return errPredicate.test(value);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * Returns an iterator containing no values, since this result is not {@link Ok}.
	 */
	@Override
	@Nonnull
	public Iterator<T> iterator() {
		return emptyIterator();
	}

	/**
	 * Returns a stream containing no values, since this result is not {@link Ok}.
	 */
	@Nonnull
	public Stream<T> stream() {
		return Stream.of();
	}
}
