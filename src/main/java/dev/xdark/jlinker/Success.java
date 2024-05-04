package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

/**
 * Success result.
 *
 * @author xDark
 */
public final class Success<V> implements Result<V> {
    private final V value;

    /**
     * @param value Value.
     */
    Success(V value) {
        this.value = value;
    }

    @Override
    public @NotNull V value() {
        return value;
    }

    @Override
    public @NotNull FailureReason failureReason() {
        throw new IllegalStateException();
    }
}
