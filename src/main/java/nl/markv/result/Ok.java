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

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

public final class Ok<T, E> implements Result<T, E> {

	private final @Nonnull T value;

	/**
	 * @see #of(T)
	 */
	Ok(@Nonnull T value) {
		requireNonNull(value, "cannot construct Ok from a null value");
		this.value = value;
	}

	/**
	 * Create a new, successful {@link Result}. Same as {@link Result#ok(T)}.
	 */
	@Nonnull
	public static <T, E> Ok<T, E> of(@Nonnull T value) {
		return new Ok<>(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOk() {
		return true;
	}

	/**
	 * Returns the value contained by this {@link Ok}. Since this can only be called ok {@link Ok} and not on
	 * {@link Result}, this is safe - it will always return a non-{@code null} result without throwing.
	 */
	@Nonnull
	public T get() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public T getOrThrow() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public T getOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier) {
		requireNonNull(exceptionSupplier);
		return value;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Marked as deprecated because calling {@link #getErrOrThrow()} on {@link Ok} always fails.
	 * 	It is not deprecated to call this on {@link Result}, but on {@link Ok} use {@link #get()}.
	 */
	@Deprecated
	@Nonnull
	@Override
	public E getErrOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier) {
		throw exceptionSupplier.get();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Marked as deprecated because calling {@link #getErrOrThrow()} on {@link Ok} always fails.
	 * 	It is not deprecated to call this on {@link Result}, but on {@link Ok} use {@link #get()}.
	 */
	@Deprecated
	@Nonnull
	@Override
	public E getErrOrThrow() {
		return getErrOrThrow(() -> new WrongResultVariantException(
				"Attempted to get Err from Result, but content is " + getUnified().toString()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <U> Result<U, E> map(@Nonnull Function<T, U> converter) {
		return Ok.of(requireNonNull(converter.apply(value)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <F> Result<T, F> mapErr(@Nonnull Function<E, F> converter) {
		requireNonNull(converter);
		return adaptErr();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ifOk(@Nonnull Consumer<T> action) {
		action.accept(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ifErr(@Nonnull Consumer<E> action) {
		requireNonNull(action);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ifEither(@Nonnull Consumer<T> okAction, @Nonnull Consumer<E> errAction) {
		requireNonNull(errAction);
		okAction.accept(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <R> R branch(@Nonnull Function<T, R> okConverter, @Nonnull Function<E, R> errHandler) {
		requireNonNull(errHandler);
		return requireNonNull(okConverter.apply(value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public T solve(@Nonnull Function<E, T> errToOkConverter) {
		requireNonNull(errToOkConverter);
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public T okOr(@Nonnull T alternative) {
		requireNonNull(alternative);
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public T okOr(@Nonnull Supplier<T> alternativeSupplier) {
		requireNonNull(alternativeSupplier);
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public E errOr(@Nonnull E alternative) {
		requireNonNull(alternative);
		return alternative;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public E errOr(@Nonnull Supplier<E> alternativeSupplier) {
		return requireNonNull(alternativeSupplier.get());
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <U> Result<U, E> adaptOk() {
		throw new WrongResultVariantException("Attempted to call 'adaptOk' on a Result containing " + toString() +
				"; this only succeeds if the Result is Err. Use 'map' to convert the Ok value.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <F> Result<T, F> adaptErr() {
		// This is implemented using a cast. It feels a bit dirty to cast something that it genuinely of
		// type Result<T, E> to a different type Result<T, F>, which is not a supertype. But generic types
		// are erased at runtime, so it works well, and it's more efficient than making a new object.

		//noinspection unchecked
		return (Result<T, F>) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public Optional<T> withoutErr() {
		return Optional.of(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public Optional<E> withoutOk() {
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <U> Result<U, E> and(@Nonnull Result<U, E> next) {
		return requireNonNull(next);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <U> Result<U, E> and(@Nonnull Supplier<Result<U, E>> nextSupplier) {
		return requireNonNull(nextSupplier.get());
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <F> Result<T, F> or(@Nonnull Result<T, F> next) {
		// See note about casting in 'adaptErr'
		//noinspection unchecked
		return (Result<T, F>) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public <F> Result<T, F> or(@Nonnull Supplier<Result<T, F>> nextSupplier) {
		// See note about casting in 'adaptErr'
		//noinspection unchecked
		return (Result<T, F>) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(@Nullable T ok) {
		if (ok == null) {
			return false;
		}
		return value.equals(ok);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsErr(@Nullable E err) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(@Nonnull Predicate<T> okPredicate) {
		return okPredicate.test(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean errMatches(@Nonnull Predicate<E> errPredicate) {
		requireNonNull(errPredicate);
		return false;
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

	/**
	 * Returns an iterator containing a single value - the content of this {@link Ok}.
	 */
	@Override
	@Nonnull
	public Iterator<T> iterator() {
		return singletonList(value).iterator();
	}

	/**
	 * Returns a stream containing a single value - the content of this {@link Ok}.
	 */
	@Override
	@Nonnull
	public Stream<T> stream() {
		return Stream.of(value);
	}
}
