package nl.markv.result;

public final record Err<T, E>(T value) implements Result<T, E> {
}
