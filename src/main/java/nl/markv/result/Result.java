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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import nl.markv.result.collect.ResultCollector;

import static java.util.Objects.requireNonNull;

/**
 * Sometimes a method can either succeed or fail. This type is one way to handle that.
 * <p>
 * A {@link Result} can either be successful (subclass {@link Ok}) or unsuccessful (subclass {@link Err}).
 * Using sealed types, the type system ensures that either one is always returned.
 * <p>
 * For example:
 * <pre>
 * var result = aFallibleMethode();
 * if (result instanceof Ok<List<Integer>, String> ok) {
 *     var value = ok.get();
 * }
 * </pre>
 * <p>
 * There are various alternatives, each of which might have their use, e.g.:
 * <ul>
 * <li> Throw a checked {@link Exception}: checked exceptions do not work so well with generics or lambdas.
 * <li> Throw an unchecked {@link RuntimeException}: it is easy to forget to handle this, so works best
 * 	if the error is likely fatal.
 * <li> Return the success result, and use a separate status code to indicate failure: very easy to forget
 * 	to check the status code, and the result and status must be kept in sync despite having different
 * 	scopes and lifetimes.
 * <li> Return a status code, and get the success result using a getter: while a bit harder to forget to
 * 	check the status, there are still pieces of data that are connected, but with different scopes and
 * 	lifetimes, making errors more likely.
 * <li> Return {@link Optional} or {@link Nullable}: this nicely connects the success and error paths, but
 * 	unfortunately it cannot contain any information about the error.
 * <li> Return a type of {@code Pair<T, E>}: can contain both the success and error types, tied together
 * 	nicely. But there is no guarantee that the pair does not contain both values, or neither.
 * </ul>
 * <p>
 * This is where {@link Result} comes in. Inspires by functional programming languages with algebraic data
 * types, it is like {@link Optional} in that it represents exactly one of two options, but it adds data to the
 * non-ok path.
 *	<p>
 * {@link Result} has many useful methods: doing things conditionally ({@link #ifOk(Consumer)},
 * {@link #contains(T)}, {@link #matches(Predicate)}), converting types into others
 * ({@link #map(Function)}, {@link #okOr(Supplier)}, {@link #solve(Function)}, {@link #adaptErr()}),
 * or combining multiple optionals ({@link #and(Supplier)}, {@link #or(Result)}, {@link ResultCollector}).
 * Creation is easy ({@link #ok(T)}, {@link #err(E)}, {@link #attempt(Attempt)},
 * {@link #from(Optional)}) and it integrates with {@link Stream}, {@link Iterable<T>} and collections
 * (e.g. {@link #transpose(List)}).
 *
 * @param <T> The type that is contained by {@link Ok} if this {@link Result} is successful.
 * @param <E> The type that is contained by {@link Err} if this {@link Result} is unsuccessful.
 */
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
	 * @see #getOrThrow(String)
	 * @see #getOrThrow(Supplier)
	 */
	@Nonnull
	T getOrThrow();

	/**
	 * If the {@link Result} is {@link Ok}, return its content, otherwise throw
	 * {@link WrongResultVariantException} with the given message.
	 *
	 * @see #getOrThrow()
	 * @see #getOrThrow(Supplier)
	 * @see #getErrOrThrow()
	 */
	@Nonnull
	T getOrThrow(@Nonnull String exceptionMessage);

	/**
	 * If the {@link Result} is {@link Ok}, return its content, otherwise throw the given exception.
	 *
	 * @throws NullPointerException if the exception supplier is called and returns {@code null}.
	 * @see #getOrThrow()
	 * @see #getOrThrow(String)
	 * @see #getErrOrThrow()
	 */
	//TODO @mark: test NPE
	@Nonnull
	T getOrThrow(@Nonnull Supplier<RuntimeException> exceptionSupplier);

	/**
	 * If the {@link Result} is {@link Err}, return its content. Otherwise, throw
	 * {@link WrongResultVariantException}.
	 * <p>
	 * This is the dual of {@link #getOrThrow()}, which describes more details.
	 *
	 * @throws WrongResultVariantException if this object is {@link Err}.
	 * @see #getOrThrow()
	 * @see #getErrOrThrow(String)
	 * @see #getErrOrThrow(Supplier)
	 */
	@Nonnull
	E getErrOrThrow();

	/**
	 * If the {@link Result} is {@link Err}, return its content, otherwise throw
	 * {@link WrongResultVariantException} with the given message.
	 *
	 * @throws NullPointerException if the exception supplier is called and returns {@code null}.
	 * @see #getOrThrow()
	 * @see #getErrOrThrow()
	 * @see #getErrOrThrow(Supplier)
	 */
	@Nonnull
	E getErrOrThrow(@Nonnull String exceptionMessage);

	/**
	 * If the {@link Result} is {@link Err}, return its content, otherwise throw the given exception.
	 *
	 * @throws NullPointerException if the exception supplier is called and returns {@code null}.
	 * @see #getOrThrow()
	 * @see #getErrOrThrow()
	 * @see #getErrOrThrow(String)
	 */
	//TODO @mark: test NPE
	@Nonnull
	E getErrOrThrow(@Nonnull Supplier<RuntimeException> exceptionSupplier);

	/**
	 * Map the {@link Ok} value to a new value of a different type. Does nothing on {@link Err}.
	 *
	 * @throws NullPointerException if the map is called and returns {@code null}.
	 * @see #mapErr(Function)
	 * @see #branch(Function, Function)
	 * @see Stream#map(Function)
	 */
	@Nonnull
	<U> Result<U, E> map(@Nonnull Function<T, U> converter);

	/**
	 * Map the {@link Ok} value to a new {@link Result} value, flattening the two results to a single one.
	 * Does nothing on {@link Err}.
	 *
	 * @see #map(Function)
	 * @see #flatMapErr(Function)
	 * @see #flatten(Result)
	 * @see Stream#flatMap(Function)
	 */
	//TODO @mark: document NPE
	@Nonnull
	<U> Result<U, E> flatMap(@Nonnull Function<T, Result<U, E>> converter);

	/**
	 * Map the {@link Err} value to a new value of a different type. Does nothing on {@link Ok}.
	 *
	 * @throws NullPointerException if the map is called and returns {@code null}.
	 * @see #map(Function)
	 * @see #branch(Function, Function)
	 * @see Stream#map(Function)
	 */
	@Nonnull
	<F> Result<T, F> mapErr(@Nonnull Function<E, F> converter);

	/**
	 * Map the {@link Err} value to a new {@link Result} value, flattening the two results to a single one.
	 * Does nothing on {@link Ok}.
	 *
	 * @see #mapErr(Function) 
	 * @see #flatMap(Function)
	 * @see #flatten(Result)
	 * @see Stream#flatMap(Function)
	 */
	//TODO @mark: document NPE
	@Nonnull
	<F> Result<T, F> flatMapErr(@Nonnull Function<E, Result<T, F>> converter);

	/**
	 * Run an action on the value of {@link Ok}. Does nothing on {@link Err}.
	 * <p>
	 * If the action is a transformation, {@link #map(Function)} should be preferred, which can take lambdas without side effects.
	 *
	 * @throws NullPointerException if the action is called and returns {@code null}.
	 * @see #map(Function)
	 * @see #ifErr(Consumer)
	 * @see #ifEither(Consumer, Consumer)
	 * @see Optional#ifPresent(Consumer)
	 */
	void ifOk(@Nonnull Consumer<T> action);

	/**
	 * Run an action on the value of {@link Err}. Does nothing on {@link Ok}.
	 * <p>
	 * If the action is a transformation, {@link #mapErr(Function)} should be preferred, which can take lambdas without side effects.
	 *
	 * @throws NullPointerException if the action is called and returns {@code null}.
	 * @see #mapErr(Function)
	 * @see #ifOk(Consumer)
	 * @see #ifEither(Consumer, Consumer)
	 */
	void ifErr(@Nonnull Consumer<E> action);

	/**
	 * Call the action for either {@link Ok} or {@link Err}.
	 *
	 * @throws NullPointerException if the action is called and returns {@code null}.
	 * @see #ifOk(Consumer)
	 * @see #ifErr(Consumer)
	 */
	void ifEither(@Nonnull Consumer<T> okAction, @Nonnull Consumer<E> errAction);

	/**
	 * Call on of the functions, depending on {@link Ok} or {@link Err}. Both should return the same type.
	 * <p>
	 * If the functions return nothing, use {@link #ifEither(Consumer, Consumer)} instead.
	 *
	 * @throws NullPointerException if either converter is called and returns {@code null}.
	 * @see #ifEither(Consumer, Consumer)
	 * @see #map(Function)
	 * @see #mapErr(Function)
	 * @see #solve(Function)
	 */
	//TODO @mark: Kotlin calls this `fold`, consider renaming
	@Nonnull
	<R> R branch(@Nonnull Function<T, R> okConverter, @Nonnull Function<E, R> errHandler);

	/**
	 * If this {@link Result} is {@link Ok}, return the value. If it is not, then map the error to something
	 * of the same type as {@link Result}, and return that.
	 * <p>
	 * If the content of {@link Err} is not needed to produce an alternative value, use {@link #okOr(Supplier)} instead.
	 *
	 * @param errToOkConverter Function that takes the type of {@link Err} and returns the type of {@link Ok}.
	 * @throws NullPointerException if the converter is called and returns {@code null}.
	 * @see #map(Function)
	 * @see #mapErr(Function)
	 * @see #branch(Function, Function)
	 * @see #okOr(Supplier)
	 */
	//TODO @mark: Kotlin calls this `recover`, consider renaming
	//TODO @mark: test NPE
	@Nonnull
	T solve(@Nonnull Function<E, T> errToOkConverter);

	/**
	 * If this {@link Result} is {@link Ok}, return the value. If it is not, return the given alternative.
	 * <p>
	 * If the result is heavy to compute, use {@link #okOr(Supplier)} or {@link #solve(Function)} instead.
	 *
	 * @param alternative The value that will replace {@link Err}.
	 * @see #okOr(Supplier)
	 * @see #solve(Function)
	 * @see #errOr(E)
	 */
	//TODO @mark: accept null
	//TODO @mark: unit test null everywhere
	@Nonnull
	T okOr(@Nonnull T alternative);

	/**
	 * If this {@link Result} is {@link Ok}, return the value. If it is not, produce an alternative using the given supplier.
	 *
	 * @param alternativeSupplier A function that will produce a value to replace {@link Err}. Will only be invoked if {@link Err}, and only once.
	 * @throws NullPointerException if the alternative supplier is called and returns {@code null}.
	 * @see #okOr(T)
	 * @see #solve(Function)
	 * @see #errOr(E)
	 */
	//TODO @mark: test NPE
	@Nonnull
	T okOr(@Nonnull Supplier<T> alternativeSupplier);

	@Nullable
	T okOrNullable(@Nullable T alternative);

	@Nullable
	T okOrNullable(@Nonnull Supplier<T> alternativeSupplier);

	@Nullable
	T okOrNull();

	/**
	 * If this {@link Result} is {@link Err}, return the value. If it is not, return the given alternative.
	 * <p>
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
	 * @throws NullPointerException if the alternative supplier is called and returns {@code null}.
	 * @see #errOr(E)
	 * @see #okOr(T)
	 */
	//TODO @mark: test NPE
	@Nonnull
	E errOr(@Nonnull Supplier<E> alternativeSupplier);

	@Nullable
	E errOrNullable(@Nullable E alternative);

	@Nullable
	E errOrNullable(@Nonnull Supplier<E> alternativeSupplier);

	@Nullable
	E errOrNull();

	/**
	 * If this result is {@link Err}, change the generic type for {@link Ok}.
	 *
	 * @throws WrongResultVariantException if called when result is {@link Ok}, as the type cannot be changed in that case. Use {@link #map(Function)} instead.
	 * @see #adaptErr() 
	 * @see #map(Function) 
	 */
	@Nonnull
	<U> Result<U, E> adaptOk();

	/**
	 * If this result is {@link Ok}, change the generic type for {@link Err}.
	 *
	 * @throws WrongResultVariantException if called when result is {@link Err}, as the type cannot be changed in that case. Use {@link #mapErr(Function)} instead.
	 * @see #adaptOk() 
	 * @see #mapErr(Function)
	 */
	@Nonnull
	<F> Result<T, F> adaptErr();

	/**
	 * Drop the {@link Err} value, replacing {@link Err} by {@link Optional#empty()} and replacing
	 * {@link Ok} by {@link Optional#of(T)}.
	 */
	@Nonnull
	Optional<T> withoutErr();

	/**
	 * Drop the {@link Ok} value, replacing {@link Err} by {@link Optional#of(E)} and replacing
	 * {@link Ok} by {@link Optional#empty()}.
	 */
	@Nonnull
	Optional<E> withoutOk();

	/**
	 * Attempt to run the given operation. Return the non-null result as {@link Ok} on success, or
	 * the {@link Exception} as {@link Err} on error.
	 *
	 * @throws NullPointerException if the attempted operation returns {@code null}.
	 */
	//TODO @mark: test NPE
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
	 * Returns the current {@link Result} if it is {@link Err}, and the next one otherwise.
	 * <p>
	 * See {@link #and(Supplier)} for more details.
	 * 
	 * @see #and(Supplier) 
	 * @see #or(Result)
	 */
	@Nonnull
	<U> Result<U, E> and(@Nonnull Result<U, E> next);

	/**
	 * Returns the current {@link Result} if it is {@link Err}, and produces the next result (given by the
	 * argument) otherwise.
	 * <p>
	 * This simulates '{@code &&}' in the sense that the result is {@link Ok} if and only if both inputs are
	 * {@link Ok}.
	 * <p>
	 * This returns the last object that had to be evaluated, like '{@code and}' in Python. I.e. if the current
	 * {@link Result} is {@link Err}, the result must be {@link Err}, and the supplier is not called.
	 *
	 * @throws NullPointerException if the supplier is called and returns {@code null}.
	 * @param nextSupplier Method that supplies the next {@link Result}. Only called (once) if current
	 * 	{@link Result} is {@link Ok}.
	 * @see #and(Result)    
	 * @see #or(Supplier)
	 */
	@Nonnull
	<U> Result<U, E> and(@Nonnull Supplier<Result<U, E>> nextSupplier);

	/**
	 * Returns the current {@link Result} if it is {@link Ok}, and the next one otherwise.
	 * <p>
	 * See {@link #or(Supplier)} for more details.
	 *
	 * @see #and(Supplier)
	 * @see #or(Result)
	 */
	@Nonnull
	<F> Result<T, F> or(@Nonnull Result<T, F> next);

	/**
	 * Returns the current {@link Result} if it is {@link Ok}, and produces the next result (given by the
	 * argument) otherwise.
	 * <p>
	 * This simulates '{@code ||}' in the sense that the result is {@link Ok} if at least one of the inputs
	 * is {@link Ok}.
	 * <p>
	 * This returns the last object that had to be evaluated, like '{@code and}' in Python. I.e. if the current
	 * {@link Result} is {@link Ok}, the result must be {@link Ok}, and the supplier is not called.
	 *
	 * @throws NullPointerException if the supplier is called and returns {@code null}.
	 * @param nextSupplier Method that supplies the next {@link Result}. Only called (once) if current
	 * 	{@link Result} is {@link Err}.
	 * @see #and(Supplier)
	 * @see #or(Result)
	 */
	@Nonnull
	<F> Result<T, F> or(@Nonnull Supplier<Result<T, F>> nextSupplier);

	/**
	 * Whether this {@link Result} is an {@link Ok} that contains an object equal to the argument.
	 *
	 * @param ok The object that will be compared against {@link Ok} using {@link Object#equals(Object)}.
	 * 	The comparison will only be done if this {@link Result} is {@link Ok}.
	 * @see #containsErr(E)
	 * @see #matches(Predicate)
	 */
	boolean contains(@Nullable T ok);

	/**
	 * Whether this {@link Result} is an {@link Err} that contains an object equal to the argument.
	 *
	 * @param err The object that will be compared against {@link Err} using {@link Object#equals(Object)}.
	 * 	The comparison will only be done if this {@link Result} is {@link Err}.
	 * @see #contains(T)
	 * @see #errMatches(Predicate)
	 */
	boolean containsErr(@Nullable E err);

	/**
	 * If this {@link Result} is {@link Ok}, the predicate will be tested against its content. If this is an
	 * {@link Err}, returns {@code false} without invoking the predicate.
	 *
	 * @see #contains(T)
	 * @see #errMatches(Predicate)
	 */
	boolean matches(@Nonnull Predicate<T> okPredicate);

	/**
	 * If this {@link Result} is {@link Err}, the predicate will be tested against its content. If this is an
	 * {@link Ok}, returns {@code false} without invoking the predicate.
	 *
	 * @see #containsErr(E)
	 * @see #matches(Predicate)
	 */
	boolean errMatches(@Nonnull Predicate<E> errPredicate);

	/**
	 * Get the content of the result, whether it is inside {@link Ok} or {@link Err}.
	 * <p>
	 * Since success and failure types are generally different, type information is lost.
	 */
	@Nonnull
	Object getUnified();

	/**
	 * Return a {@link Stream}, containing either a single {@link Ok} value if this result is successful,
	 * or containing nothing if it is an {@link Err}.
	 * <p>
	 * There are also {@link Collectors}s for streams of {@link Result}s, for example {@link ResultCollector#toList()}.
	 *
	 * @see ResultCollector
	 * @see #iterator()
	 */
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

	/**
	 * Flatten a nested result. Returns the inner {@link Ok} if both are successful. Otherwise, returns the
	 * {@link Err} of whichever one failed (the errors must be the same type).
	 * 
	 * @see #flatMap(Function)
	 */
	@Nonnull
	@CheckReturnValue
	static <U, F> Result<U, F> flatten(@Nonnull Result<Result<U, F>, F> result) {
		if (result instanceof Ok<Result<U, F>, F> ok) {
			return ok.get();
		}
		return result.adaptOk();
	}
}
