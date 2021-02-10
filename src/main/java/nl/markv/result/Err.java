package nl.markv.result;

import java.util.function.Supplier;

public final record Err<T, E>(E value) implements Result<T, E> {

	public static <T, E> Err<T, E> of(E value) {
		return new Err<>(value);
	}

	@Override
	public boolean isOk() {
		return false;
	}

	public E get() {
		return value;
	}

	@Override
	public T getOrThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
		throw exceptionSupplier.get();
	}

	@Override
	public E getErrOrThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
		return value;
	}

	@Override
	public Object getUnified() {
		return value;
	}
}
