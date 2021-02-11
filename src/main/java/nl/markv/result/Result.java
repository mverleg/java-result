package nl.markv.result;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

public sealed interface Result<T, E> permits Ok, Err {

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

	//TODO @mark: test all:

	@Nonnull
	default T getOrThrow() {
		return getOrThrow(() -> new WrongResultVariantException("Attempted to get Ok from Result, but content is Err(" + getUnified().toString() + ")"));
	}

	T getOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier);

	@Nonnull
	default E getErrOrThrow() {
		return getErrOrThrow(() -> new WrongResultVariantException("Attempted to get Err from Result, but content is Ok(" + getUnified().toString() + ")"));
	}

	E getErrOrThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier);

	/**
	 * Get the content of the result, whether it is inside {@link Ok} or {@link Err}.
	 *
	 * Since success and failure types are in general different, type information is lost.
	 */
	@Nonnull
	Object getUnified();
}
