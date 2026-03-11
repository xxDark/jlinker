package dev.xdark.jlinker;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MethodResolutionException extends Exception {
	private final MethodResolutionViolation violation;

	MethodResolutionException(MethodResolutionViolation violation) {
		super("Resolution violation: %s".formatted(violation), null, false, false);
		this.violation = violation;
	}

	public MethodResolutionViolation violation() {
		return violation;
	}
}
