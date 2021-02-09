package nl.markv.result;

public sealed interface Result<T, E> permits Ok, Err {

	static <T, E> Ok<T, E> ok(T value) {
		return new Ok<>(value);
	}

	static <T, E> Err<T, E> err(E value) {
		return new Err<>(value);
	}
}
