package nl.markv.result;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Like {@link Supplier}, but can throw checked exceptions.
 */
@FunctionalInterface
public interface Attempt<T> {
	@Nonnull
	T attempt() throws Exception;
}
