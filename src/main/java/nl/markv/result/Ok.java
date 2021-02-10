package nl.markv.result;

import java.util.function.Supplier;

//TODO @mark: nn
public final record Ok<T, E>(T value) implements Result<T, E> {

	public static <T, E> Ok<T, E> of(T value) {
		return new Ok<>(value);
	}

	@Override
	public boolean isOk() {
		return true;
	}

	public T get() {
		return value;
	}

	@Override
	public T getOrThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
		return value;
	}

	@Override
	public E getErrOrThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
		throw exceptionSupplier.get();
	}

	@Override
	public Object getUnified() {
		return value;
	}
}
