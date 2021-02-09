package nl.markv.result;

public final record Ok<T, E>(T value) implements Result<T, E> {
}
