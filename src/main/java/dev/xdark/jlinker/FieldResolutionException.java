package dev.xdark.jlinker;

import org.jspecify.annotations.NullMarked;

/// Thrown by [LinkResolver] when a field resolution attempt fails.
///
/// The [#violation()] method returns the specific [FieldResolutionViolation]
/// constant that describes why resolution failed, enabling precise error
/// handling without string parsing.
@NullMarked
public final class FieldResolutionException extends Exception {
	private final FieldResolutionViolation violation;

	FieldResolutionException(FieldResolutionViolation violation) {
		super("Resolution violation: %s".formatted(violation), null, false, false);
		this.violation = violation;
	}

	/// Returns the specific constraint that was violated during resolution.
	public FieldResolutionViolation violation() {
		return violation;
	}
}
