package nl.markv.result;

import javax.annotation.Nonnull;

/**
 * A very simple, empty class to represent 'nothing'.
 * <p>
 * It only has one singleton instance, {@link #none}.
 */
@SuppressWarnings("InstantiationOfUtilityClass")
public final class None {
	public static final None none = new None();

	private None() {}

	@Nonnull
	public static None get() {
		return none;
	}
}
