package dev.xdark.jlinker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Error<V> implements Result<V> {
	private static final List<Error<?>> ERRORS = Arrays.stream(FailureReason.values())
			.map(Error::new)
			.collect(Collectors.toList());
	private final FailureReason reason;

	Error(FailureReason reason) {
		this.reason = reason;
	}

	@Override
	public V getValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FailureReason getFailureReason() {
		return reason;
	}

	static <V> Error<V> of(FailureReason reason) {
		return (Error<V>) ERRORS.get(reason.ordinal());
	}
}
