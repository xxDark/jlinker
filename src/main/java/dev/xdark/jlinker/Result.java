package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

/**
 * Resolution result.
 *
 * @author xDark
 */
public sealed interface Result<V> permits Success, Error {

	/**
	 * @return Resolution result.
	 * @throws IllegalStateException If in error state.
	 */
	@NotNull
	V value();

	/**
	 * @return Resolution result.
	 * @throws IllegalStateException If in success state.
	 */
	@NotNull
	FailureReason failureReason();
}
