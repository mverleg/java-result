package nl.markv.result;

import javax.annotation.Nonnull;

@SuppressWarnings("InstantiationOfUtilityClass")
public final class None {
	public static final None none = new None();

	private None() {}

	@Nonnull
	public static None create() {
		return none;
	}
}
