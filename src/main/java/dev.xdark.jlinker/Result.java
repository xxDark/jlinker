package dev.xdark.jlinker;

/**
 * Result.
 *
 * @author xDark
 */
public interface Result<V> {

    /**
     * @return Value.
     */
    V value();

    /**
     * @return Error, if action had failed.
     */
    ResolutionError error();

    /**
     * @return Whether the action performed successfully.
     */
    boolean isSuccess();

    /**
     * @return Whether the action performed unsuccessfully.
     */
    default boolean isError() {
        return !isSuccess();
    }

    static <V> Result<V> ok(V value) {
        return new Success<>(value);
    }

    static <V> Result<V> error(ResolutionError error) {
        return new Error<>(error);
    }
}
