package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

/**
 * Arena allocator.
 *
 * @author xDark
 */
public interface ArenaAllocator<T> {

    /**
     * @return Allocated arena.
     */
    @NotNull Arena<T> push();
}
