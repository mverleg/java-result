package nl.markv.result;

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

	public static <T, R> R failIfCalled(T ignored) {
		throw new AssertionError();
	}
}
