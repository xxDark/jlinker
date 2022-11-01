package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

/**
 * Result.
 *
 * @author xDark
 */
public interface Result<V> {

    /**
     * @return Value.
     */
    @NotNull V value();

    /**
     * @return Error, if action had failed.
     */
    @NotNull ResolutionError error();

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

    static <V> Result<V> ok(@NotNull V value) {
        return new Success<>(value);
    }

    static <V> Result<V> error(@NotNull ResolutionError error) {
        return new Error<>(error);
    }
}
