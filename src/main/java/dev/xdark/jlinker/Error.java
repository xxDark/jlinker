package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

/**
 * Error result.
 *
 * @author xDark
 */
final class Error<V> implements Result<V> {
    private final ResolutionError error;

    /**
     * @param error
     *      Error.
     */
    Error(ResolutionError error) {
        this.error = error;
    }

    @Override
    public @NotNull V value() {
        throw new IllegalStateException(error.name());
    }

    @Override
    public @NotNull ResolutionError error() {
        return error;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }
}
