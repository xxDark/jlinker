package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public final class Error<V> implements Result<V> {
	private static final List<Error<?>> ERRORS = Arrays.stream(FailureReason.values())
			.<Error<?>>map(Error::new)
			.toList();
	private final FailureReason reason;

	Error(FailureReason reason) {
		this.reason = reason;
	}

	@Override
	public @NotNull V value() {
		throw new IllegalStateException();
	}

	@Override
	public @NotNull FailureReason failureReason() {
		return reason;
	}

	static <V> Error<V> of(FailureReason reason) {
		//noinspection unchecked
		return (Error<V>) ERRORS.get(reason.ordinal());
	}
}
