package nl.markv.result;

/**
 * Thrown when code tries to get {@link Ok} from a {@link Result} that failed, or {@link Err}
 * from a {@link Result} that succeeded.
 */
public final class WrongResultVariantException extends Exception {

	public WrongResultVariantException(String message) {
		super(message);
	}
}
