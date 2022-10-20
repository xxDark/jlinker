package dev.xdark.jlinker;

import java.util.Arrays;

/**
 * Frame-based arena allocator.
 *
 * @author xDark
 */
public final class FrameArenaAllocator<T> implements ArenaAllocator<T>, Arena<T> {
    private static final Object[] EMPTY_ARRAY = {};

    private int[] frames = new int[16];
    private int frameIndex;
    private T[] cache = (T[]) EMPTY_ARRAY;
    private int index;

    @Override
    public Arena<T> push() {
        // Push new frame.
        int[] frames = this.frames;
        int nextFrame = frameIndex++;
        if (nextFrame == frames.length) {
            frames = Arrays.copyOf(frames, nextFrame + 16);
            this.frames = frames;
        }
        frames[nextFrame] = index;
        return this;
    }

    @Override
    public void close() {
        index = frames[--frameIndex];
    }

    @Override
    public void push(T value) {
        T[] cache = this.cache;
        int index = this.index++;
        if (index == cache.length) {
            cache = Arrays.copyOf(cache, index + 16);
            this.cache = cache;
        }
        cache[index] = value;
    }

    @Override
    public T poll() {
        int index = this.index;
        if (index == frames[frameIndex - 1]) {
            return null;
        }
        return cache[this.index = --index];
    }
}
