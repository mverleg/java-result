package nl.markv.result;

//TODO @mark: nn
public final record Ok<T, E>(T value) implements Result<T, E> {

	public static <T, E> Ok<T, E> of(T value) {
		return new Ok<>(value);
	}

	@Override
	public boolean isOk() {
		return true;
	}

	@Override
	public Object getUnified() {
		return value;
	}

	public T get() {
		return value;
	}
}
