package nl.markv.result;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;
import static nl.markv.result.None.none;

//TODO @mark: Collection<Result<.>> to Result<Collection<.>>
//TODO @mark: Result<Result<.>> to Result<.>, if possible with generics
//TODO @mark: make Ok and Err refer to documentation on parent method
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

	//TODO @mark: test
	@Nonnull
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	static <T> Result<T, None> from(@Nonnull Optional<T> optional) {
		if (optional.isPresent()) {
			return Ok.of(optional.get());
		}
		return Err.empty();
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

	//TODO @mark: test null
	@Nonnull
	<U> Result<U, E> map(@Nonnull Function<T, U> converter);

	@Nonnull
	<F> Result<T, F> mapErr(@Nonnull Function<E, F> converter);

	void ifOk(@Nonnull Consumer<T> action);

	void ifErr(@Nonnull Consumer<E> action);

	void branch(@Nonnull Consumer<T> okAction, Consumer<E> errAction);

	//TODO @mark: make sure type inference can distinguish these lambdas in most cases, otherwise rename
	@Nonnull
	<G> G branch(@Nonnull Function<T, G> okConverter, Function<E, G> errHandler);

	/**
	 * If this {@link Result} is {@link Ok}, return the value. If it is not, then map the error to something
	 * of the same type as {@link Result}, and return that.
	 */
	@Nonnull
	T solve(Function<E, T> errToOkConverter);

	//TODO @mark: test null everywhere
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

	/**
	 * @see #and(Supplier)
	 */
	@Nonnull
	<U> Result<U, E> and(@Nonnull Result<U, E> next);

	/**
	 * Returns the current {@link Result} if it is {@link Err}, and produces the next result (given by
	 * argument) otherwise.
	 * <p>
	 * This simulates '{@code &&}' in the sense that the result is {@link Ok} if and only if both inputs are
	 * {@link Ok}. The supplier is only called if the current object is {@link Ok}.
	 * <p>
	 * This returns the last object that had to be evaluated, like '{@code and}' in Python.
	 */
	@Nonnull
	<U> Result<U, E> and(@Nonnull Supplier<Result<U, E>> nextSupplier);

	/**
	 * @see #or(Supplier)
	 */
	@Nonnull
	<F> Result<T, F> or(@Nonnull Result<T, F> next);

	/**
	 * Returns the current {@link Result} if it is {@link Ok}, and produces the next result (given by
	 * argument) otherwise.
	 * <p>
	 * This simulates '{@code ||}' in the sense that the result is {@link Ok} if at least one of the inputs
	 * is {@link Ok}. The supplier is only called if the current object is {@link Err}.
	 * <p>
	 * This returns the last object that had to be evaluated, like '{@code or}' in Python.
	 */
	@Nonnull
	<F> Result<T, F> or(@Nonnull Supplier<Result<T, F>> nextSupplier);

	boolean contains(@Nullable T ok);

	boolean containsErr(@Nullable E err);

	/**
	 * Get the content of the result, whether it is inside {@link Ok} or {@link Err}.
	 * <p>
	 * Since success and failure types are generally different, type information is lost.
	 */
	@Nonnull
	Object getUnified();

	@Nonnull
	Stream<T> stream();

	//TODO @mark: test all the transposes
	//TODO @mark: collector like this for streams:
	/**
	 * Given a list of results, if it contains an error, return the first one. If there are no errors,
	 * return a list of all the success values.
	 */
	@Nonnull
	@CheckReturnValue
	//TODO @mark:
	static <U, F> Result<List<U>, F> transpose(@Nonnull List<Result<U, F>> resultList) {
		final List<U> okList = new ArrayList<>(resultList.size());
		for (Result<U, F> item : resultList) {
			if (item instanceof Ok<U, F> ok) {
				okList.add(ok.get());
			} else {
				return item.adaptOk();
			}
		}
		return Ok.of(okList);
	}

	/**
	 * Given a set of results, if it contains any errors, return the 'first' one ('first' may be arbitrary
	 * for many {@link Set} implementation). If there are no errors, return a set of all the success values.
	 */
	@Nonnull
	@CheckReturnValue
	static <U, F> Result<Set<U>, F> transpose(@Nonnull Set<Result<U, F>> resultSet) {
		final Set<U> okSet = new LinkedHashSet<>((int)(resultSet.size() / 0.7 + 1), 0.7f);
		for (Result<U, F> item : resultSet) {
			if (item instanceof Ok<U, F> ok) {
				okSet.add(ok.get());
			} else {
				return item.adaptOk();
			}
		}
		return Ok.of(okSet);
	}

	//TODO @mark: javadoc
	/**
	 * Transform {@link Optional} of {@link Result} to {@link Result} of {@link Optional}, by keeping errors
	 * <ol>
	 * <li> {@code Ok(empty)} to {@code Ok(empty)}
	 * <li> {@code Ok(empty)} to {@code Ok(empty)}
	 * </ol>
	 */
	@Nonnull
	@CheckReturnValue
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	static <U, F> Result<Optional<U>, F> transpose(@Nonnull Optional<Result<U, F>> optionalResult) {
		if (optionalResult.isEmpty()) {
			return Ok.of(Optional.empty());
		}
		Result<U, F> result = optionalResult.get();
		if (result instanceof Ok<U, F> ok) {
			return Ok.of(Optional.of(ok.get()));
		} else if (result instanceof Err<U, F> err) {
			return Err.of(err.get());
		} else {
			throw new IllegalStateException("unreachable");
		}
// 		Use this switch expression?
//		return switch (result) {
//			case Ok ok -> Ok.of(Optional.of(ok.get()));
//			case Err err -> Err.of(err.get());
//		};
	}

	//TODO @mark: javadoc
	@Nonnull
	@CheckReturnValue
	static <U, F> Optional<Result<U, F>> transpose(@Nonnull Result<Optional<U>, F> resultOptional) {
		if (resultOptional instanceof Ok<Optional<U>, F> ok) {
			return ok.get().map(Ok::of);
		} else if (resultOptional instanceof Err<Optional<U>, F> err) {
			return Optional.of(err.adaptOk());
		} else {
			throw new IllegalStateException("unreachable");
		}
	}

	@Nonnull
	@CheckReturnValue
	static <U, F> Result<U, F> flatten(@Nonnull Result<Result<U, F>, F> result) {
		if (result instanceof Ok<Result<U, F>, F> ok) {
			return ok.get();
		}
		return result.adaptOk();
	}
}
