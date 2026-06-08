package dev.xdark.jlinker;

import org.jspecify.annotations.NullMarked;

/// Thrown by [LinkResolver] when a method resolution attempt fails.
///
/// The [#violation()] method returns the specific [MethodResolutionViolation]
/// constant that describes why resolution failed, enabling precise error
/// handling without string parsing.
@NullMarked
public final class MethodResolutionException extends Exception {
	private final MethodResolutionViolation violation;

	MethodResolutionException(MethodResolutionViolation violation) {
		super("Resolution violation: %s".formatted(violation), null, false, false);
		this.violation = violation;
	}

	/// Returns the specific constraint that was violated during resolution.
	public MethodResolutionViolation violation() {
		return violation;
	}
}
