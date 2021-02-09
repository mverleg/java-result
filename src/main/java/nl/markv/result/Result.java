package nl.markv.result;

import java.util.function.Supplier;

public sealed interface Result<T, E> permits Ok, Err {

	static <T, E> Ok<T, E> ok(T value) {
		return new Ok<>(value);
	}

	static <T, E> Err<T, E> err(E value) {
		return new Err<>(value);
	}

	boolean isOk();

	default boolean isErr() {
		return !isOk();
	}

	default T getOrThrow() {
		return getOrThrow(() -> new WrongResultVariantException("Attempted to get Ok from Result, but content is Err(" + getUnified().toString() + ")"));
	}

	T getOrThrow(Supplier<? extends Exception> exceptionSupplier);

	default E getErrOrThrow() {
		return getErrOrThrow(() -> new WrongResultVariantException("Attempted to get Err from Result, but content is Ok(" + getUnified().toString() + ")"));
	}

	E getErrOrThrow(Supplier<? extends Exception> exceptionSupplier);

	/**
	 * Get the content of the result, whether it is inside {@link Ok} or {@link Err}.
	 *
	 * Since success and failure types are in general different, type information is lost.
	 */
	Object getUnified();
}
