package nl.markv.result;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

//TODO @mark: @Nonnull everywhere, and requireNonNull for arguments
public sealed interface Result<T, E> extends Iterable<T> permits Ok, Err {

	@Nonnull
	static <T, E> Ok<T, E> ok(@Nonnull T value) {
		return new Ok<>(value);
	}

	@Nonnull
	static <T, E> Err<T, E> err(@Nonnull E value) {
		return new Err<>(value);
	}

	boolean isOk();

	default boolean isErr() {
		return !isOk();
	}

	@Nonnull
	default T getOrThrow() {
		return getOrThrow(() -> new WrongResultVariantException(
				"Attempted to get Ok from Result, but content is " + toString()));
	}

	@Nonnull
	T getOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier);

	@Nonnull
	default E getErrOrThrow() {
		return getErrOrThrow(() -> new WrongResultVariantException(
				"Attempted to get Err from Result, but content is " + getUnified().toString()));
	}

	@Nonnull
	E getErrOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier);

	@Nonnull
	<U> Result<U, E> map(@Nonnull Function<T, U> converter);

	@Nonnull
	<F> Result<T, F> mapErr(@Nonnull Function<E, F> converter);

	@Nonnull
	T okOr(@Nonnull T alternative);

	@Nonnull
	T okOr(@Nonnull Supplier<T> alternativeSupplier);

	@Nonnull
	E errOr(@Nonnull E alternative);

	@Nonnull
	E errOr(@Nonnull Supplier<E> alternativeSupplier);

	/**
	 * Dual of {@link #adaptErr()}.
	 */
	@Nonnull
	<U> Result<U, E> adaptOk();

	/**
	 * If this result is {@link Ok}, re-wrap the same value  with a different generic type for {@link Err}.
	 * This will throw {@link WrongResultVariantException} if called on an {@link Err}, as the type cannot be changed in that case.
	 */
	@Nonnull
	<F> Result<T, F> adaptErr();

	@Nonnull
	Optional<T> withoutErr();

	@Nonnull
	Optional<E> withoutOk();

	/**
	 * Attempt to run the given operation. Return the non-null result as {@link Ok} on success, or the
	 * {@link Exception} as {@link Err} on error.
	 */
	@Nonnull
	static <U> Result<U, Exception> attempt(@Nonnull Attempt<U> attemptedOperation) {
		try {
			return Ok.of(requireNonNull(attemptedOperation.attempt(),
					"Operation for 'attempt' must not return null"));
		} catch (Exception exception) {
			return Err.of(exception);
		}
	}

	boolean contains(@Nullable T ok);

	boolean containsErr(@Nullable E err);

	/**
	 * Get the content of the result, whether it is inside {@link Ok} or {@link Err}.
	 *
	 * Since success and failure types are generally different, type information is lost.
	 */
	@Nonnull
	Object getUnified();

	@Nonnull
	Stream<T> stream();
}
