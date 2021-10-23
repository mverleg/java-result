package nl.markv.result;

import javax.annotation.Nullable;

public class TestUtil {

	public static final class Toggle {
		boolean state = false;

		public void turnOn() {
			state = true;
		}

		public boolean isOn() {
			return state;
		}
	}

	public static <R> R failIfCalled() {
		throw new AssertionError();
	}

	@Nullable
	public static <R> R nullSupplier() {
		return null;
	}

	public static <T, R> R failIfCalled(@SuppressWarnings("unused") T ignored) {
		throw new AssertionError();
	}
}
