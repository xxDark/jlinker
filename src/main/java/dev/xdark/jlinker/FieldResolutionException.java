package dev.xdark.jlinker;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class FieldResolutionException extends Exception {
	private final FieldResolutionViolation violation;

	FieldResolutionException(FieldResolutionViolation violation) {
		super("Resolution violation: %s".formatted(violation), null, false, false);
		this.violation = violation;
	}

	@NotNull
	@Contract(pure = true)
	public FieldResolutionViolation violation() {
		return violation;
	}
}
