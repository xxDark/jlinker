package dev.xdark.jlinker;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class MethodResolutionException extends Exception {
	private final MethodResolutionViolation violation;

	MethodResolutionException(MethodResolutionViolation violation) {
		super("Resolution violation: %s".formatted(violation), null, false, false);
		this.violation = violation;
	}

	@NotNull
	@Contract(pure = true)
	public MethodResolutionViolation violation() {
		return violation;
	}
}
