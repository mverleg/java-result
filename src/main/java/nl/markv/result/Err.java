package nl.markv.result;

public final record Err<T, E>(E value) implements Result<T, E> {

	public static <T, E> Err<T, E> of(E value) {
		return new Err<>(value);
	}

	@Override
	public boolean isOk() {
		return false;
	}

	@Override
	public Object getUnified() {
		return value;
	}

	public E get() {
		return value;
	}
}
