package nl.markv.result;

public final record Ok<T, E>(T value) implements Result<T, E> {

	public static <T, E> Ok<T, E> of(T value) {
		return new Ok<>(value);
	}
}
