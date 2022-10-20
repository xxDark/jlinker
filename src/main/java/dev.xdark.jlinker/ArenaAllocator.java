package dev.xdark.jlinker;

/**
 * Arena allocator.
 *
 * @author xDark
 */
public interface ArenaAllocator<T> {

    /**
     * @return Allocated arena.
     */
    Arena<T> push();
}
