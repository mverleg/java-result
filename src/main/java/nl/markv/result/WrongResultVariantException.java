package nl.markv.result;

import javax.annotation.Nonnull;

/**
 * Thrown when code tries to get {@link Ok} from a {@link Result} that failed, or {@link Err}
 * from a {@link Result} that succeeded.
 */
public final class WrongResultVariantException extends RuntimeException {

	public WrongResultVariantException(@Nonnull String message) {
		super(message);
	}
}
