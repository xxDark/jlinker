package dev.xdark.jlinker;

public interface Result<V> {

	V getValue();

	FailureReason getFailureReason();
}
