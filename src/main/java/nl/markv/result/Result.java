package nl.markv.result;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import nl.markv.result.collect.ResultCollector;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

//TODO @mark: make Ok and Err refer to documentation on parent method
//TODO @mark: @Nonnull everywhere, and requireNonNull for arguments
public sealed interface Result<T, E> extends Iterable<T> permits Ok, Err {

	/**
	 * Create a successful {@link Result}.
	 */
	@Nonnull
	static <T, E> Ok<T, E> ok(@Nonnull T value) {
		return new Ok<>(value);
	}

	/**
	 * Create an unsuccessful {@link Result}.
	 */
	@Nonnull
	static <T, E> Err<T, E> err(@Nonnull E value) {
		return new Err<>(value);
	}

	/**
	 * Create a {@link Result} from an {@link Optional}. Non-empty values become {@link Ok}, and empty
	 * values become {@link Err} of type {@link None}.
	 */
	@Nonnull
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	static <T> Result<T, None> from(@Nonnull Optional<T> optional) {
		if (optional.isPresent()) {
			return Ok.of(optional.get());
		}
		return Err.empty();
	}

	/**
	 * Create a {@link Result} from a nullable reference. Non-null values become {@link Ok}, and null values
	 * become {@link Err} of type {@link None}.
	 */
	@Nonnull
	static <T> Result<T, None> fromNullable(@Nullable T value) {
		if (value != null) {
			return Ok.of(value);
		}
		return Err.empty();
	}

	/**
	 * Whether the {@link Result} is {@link Ok}.
	 */
	boolean isOk();

	/**
	 * Whether the {@link Result} is {@link Err}.
	 */
	default boolean isErr() {
		return !isOk();
	}

	/**
	 * If the {@link Result} is {@link Ok}, return its content. Otherwise, throw {@link WrongResultVariantException}.
	 * <p>
	 * Note that it may be better to get the okay value without chance of exceptions:
	 * <br/>
	 * <pre>
	 * if (result instanceof Ok<List<Integer>, String> ok) {
	 *     var value = ok.get();
	 * }
	 * </pre>
	 *
	 * @throws WrongResultVariantException if this object is {@link Err}.
	 * @see #getErrOrThrow()
	 * @see #getOrThrow(Supplier)
	 */
	@Nonnull
	default T getOrThrow() {
		return getOrThrow(() -> new WrongResultVariantException(
				"Attempted to get Ok from Result, but content is " + toString()));
	}

	/**
	 * If the {@link Result} is {@link Ok}, return its content, otherwise throw the given exception.
	 *
	 * @see #getOrThrow()
	 * @see #getErrOrThrow()
	 */
	@Nonnull
	T getOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier);

	/**
	 * If the {@link Result} is {@link Err}, return its content. Otherwise, throw {@link WrongResultVariantException}.
	 * <p>
	 * This is the dual of {@link #getOrThrow()}, which describes more details.
	 *
	 * @throws WrongResultVariantException if this object is {@link Err}.
	 * @see #getOrThrow()
	 * @see #getErrOrThrow(Supplier)
	 */
	@Nonnull
	default E getErrOrThrow() {
		return getErrOrThrow(() -> new WrongResultVariantException(
				"Attempted to get Err from Result, but content is " + getUnified().toString()));
	}

	/**
	 * If the {@link Result} is {@link Err}, return its content, otherwise throw the given exception.
	 *
	 * @see #getOrThrow()
	 * @see #getErrOrThrow()
	 */
	@Nonnull
	E getErrOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier);

	/**
	 * Map the {@link Ok} value to a new value of a different type. Does nothing on {@link Err}.
	 *
	 * @see #mapErr(Function)
	 * @see #branch(Function, Function)
	 * @see Stream#map(Function)
	 */
	@Nonnull
	<U> Result<U, E> map(@Nonnull Function<T, U> converter);

	/**
	 * Map the {@link Err} value to a new value of a different type. Does nothing on {@link Ok}.
	 *
	 * @see #map(Function)
	 * @see #branch(Function, Function)
	 * @see Stream#map(Function)
	 */
	@Nonnull
	<F> Result<T, F> mapErr(@Nonnull Function<E, F> converter);

	/**
	 * Run an action on the value of {@link Ok}. Does nothing on {@link Err}.
	 *
	 * If the action is a transformation, {@link #map(Function)} should be preferred, which can take lambdas without side effects.
	 *
	 * @see #map(Function)
	 * @see #ifErr(Consumer)  
	 * @see #ifEither(Consumer, Consumer)
	 * @see Optional#ifPresent(Consumer) 
	 */
	void ifOk(@Nonnull Consumer<T> action);

	/**
	 * Run an action on the value of {@link Err}. Does nothing on {@link Ok}.
	 *
	 * If the action is a transformation, {@link #mapErr(Function)} should be preferred, which can take lambdas without side effects.
	 *
	 * @see #mapErr(Function) 
	 * @see #ifOk(Consumer)
	 * @see #ifEither(Consumer, Consumer)
	 */
	void ifErr(@Nonnull Consumer<E> action);

	/**
	 * Call the action for either {@link Ok} or {@link Err}.
	 *
	 * @see #ifOk(Consumer)
	 * @see #ifErr(Consumer) 
	 */
	void ifEither(@Nonnull Consumer<T> okAction, @Nonnull Consumer<E> errAction);

	/**
	 * Call on of the functions, depending on {@link Ok} or {@link Err}. Both should return the same type.
	 * 
	 * If the functions return nothing, use {@link #ifEither(Consumer, Consumer)} instead.
	 *
	 * @see #ifEither(Consumer, Consumer)
	 * @see #map(Function)
	 * @see #mapErr(Function)
	 * @see #solve(Function)
	 */
	@Nonnull
	<R> R branch(@Nonnull Function<T, R> okConverter, @Nonnull Function<E, R> errHandler);

	/**
	 * If this {@link Result} is {@link Ok}, return the value. If it is not, then map the error to something
	 * of the same type as {@link Result}, and return that.
	 *
	 * If the content of {@link Err} is not needed to produce an alternative value, use {@link #okOr(Supplier)} instead.
	 *
	 * @param errToOkConverter Function that takes the type of {@link Err} and returns the type of {@link Ok}.
	 * @see #map(Function)
	 * @see #mapErr(Function)
	 * @see #branch(Function, Function)
	 * @see #okOr(Supplier)
	 */
	@Nonnull
	T solve(@Nonnull Function<E, T> errToOkConverter);

	/**
	 * If this {@link Result} is {@link Ok}, return the value. If it is not, return the given alternative.
	 *
	 * If the result is heavy to compute, use {@link #okOr(Supplier)} or {@link #solve(Function)} instead.
	 *
	 * @param alternative The value that will replace {@link Err}.
	 * @see #okOr(Supplier)
	 * @see #solve(Function)
	 * @see #errOr(T)
	 */
	//TODO @mark: accept null
	//TODO @mark: unit test null everywhere
	@Nonnull
	T okOr(@Nonnull T alternative);

	/**
	 * If this {@link Result} is {@link Ok}, return the value. If it is not, produce an alternative using the given supplier.
	 *
	 * @param alternativeSupplier A function that will produce a value to replace {@link Err}. Will only be invoked if {@link Err}, and only once.
	 * @see #okOr(T)
	 * @see #solve(Function)
	 * @see #errOr(T)
	 */
	@Nonnull
	T okOr(@Nonnull Supplier<T> alternativeSupplier);

	/**
	 * If this {@link Result} is {@link Err}, return the value. If it is not, return the given alternative.
	 *
	 * If the result is heavy to compute, use {@link #errOr(Supplier)} instead.
	 *
	 * @param alternative The value that will replace {@link Ok}.
	 * @see #errOr(Supplier)
	 * @see #okOr(T)
	 */
	@Nonnull
	E errOr(@Nonnull E alternative);

	/**
	 * If this {@link Result} is {@link Err}, return the value. If it is not, produce an alternative using the given supplier.
	 *
	 * @param alternativeSupplier A function that will produce a value to replace {@link Ok}. Will only be invoked if {@link Ok}, and only once.
	 * @see #errOr(T)
	 * @see #okOr(T)
	 */
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

	boolean matches(@Nonnull Predicate<T> okPredicate);

	boolean errMatches(@Nonnull Predicate<E> errPredicate);

	/**
	 * Get the content of the result, whether it is inside {@link Ok} or {@link Err}.
	 * <p>
	 * Since success and failure types are generally different, type information is lost.
	 */
	@Nonnull
	Object getUnified();

	@Nonnull
	Stream<T> stream();

	/**
	 * Given a list of results, if it contains an error, return the first one. If there are no errors,
	 * return a list of all the success values.
	 *
	 * See {@link ResultCollector#toList()} for the stream equivalent.
	 */
	@Nonnull
	@CheckReturnValue
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
	 *
	 * See {@link ResultCollector#toSet()} for the stream equivalent.
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

	/**
	 * Transform {@link Optional} of {@link Result} to {@link Result} of {@link Optional}:
	 * <ol>
	 * <li> {@code Some(Ok(x))} to {@code Ok(Some(x))}
	 * <li> {@code Some(Err(y))} to {@code Err(y)}
	 * <li> {@code None} to {@code Ok(None)}
	 * </ol>
	 *
	 * See {@link #transpose(Result)} for the inverse.
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
	}

	/**
	 * Transform {@link Result} of {@link Optional} to {@link Optional} of {@link Result}:
	 * <ol>
	 * <li> {@code Ok(Some(x))} to {@code Some(Ok(x))}
	 * <li> {@code Ok(None)} to {@code None}
	 * <li> {@code Err(y)} to {@code Some(Err(y))}
	 * </ol>
	 *
	 * See {@link #transpose(Optional)} for the inverse.
	 */
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
