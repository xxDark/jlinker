package dev.xdark.jlinker;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FieldResolutionException extends Exception {
	private final FieldResolutionViolation violation;

	FieldResolutionException(FieldResolutionViolation violation) {
		super("Resolution violation: %s".formatted(violation), null, false, false);
		this.violation = violation;
	}

	public FieldResolutionViolation violation() {
		return violation;
	}
}
