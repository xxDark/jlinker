package dev.xdark.jlinker;

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

    void push(T value);

    T poll();

    default void push(Collection<? extends T> c) {
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
