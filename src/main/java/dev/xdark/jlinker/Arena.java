package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Collection;

/**
 * Arena cache.
 * Arena is a growing, in capacity,
 * collection that keeps track of added/removed elements,
 * like {@link java.util.Deque}.
 *
 * @author xDark
 */
public interface Arena<T> extends Closeable {

    /**
     * @param value Value to push.
     */
    void push(@NotNull T value);

    /**
     * @return Top value or {@code null}.
     */
    @Nullable T poll();

    /**
     * @param c Collection of values to push.
     */
    default void push(@NotNull Collection<? extends T> c) {
        if (!c.isEmpty()) {
            for (T t : c) {
                push(t);
            }
        }
    }

    /**
     * Cleans up this arena.
     */
    @Override
    void close();
}
