package nl.markv.result;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface Attempt<T> {
	@Nonnull
	T attempt() throws Exception;
}
