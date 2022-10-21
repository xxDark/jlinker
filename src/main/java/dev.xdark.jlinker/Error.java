package dev.xdark.jlinker;

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
    public V value() {
        throw new IllegalStateException(error.name());
    }

    @Override
    public ResolutionError error() {
        return error;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }
}
