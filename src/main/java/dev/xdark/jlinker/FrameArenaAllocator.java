package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Frame-based arena allocator.
 *
 * @author xDark
 */
public final class FrameArenaAllocator<T> implements ArenaAllocator<T> {
    private static final Object[] EMPTY_ARRAY = {};

    private int[] frames = new int[16];
    private int frameIndex;
    private final Impl impl = new Impl();

    @Override
    public @NotNull Arena<T> push() {
        // Push new frame.
        int[] frames = this.frames;
        int nextFrame = frameIndex++;
        if (nextFrame == frames.length) {
            frames = Arrays.copyOf(frames, nextFrame + 16);
            this.frames = frames;
        }
        Impl impl = this.impl;
        frames[nextFrame] = impl.index;
        return impl;
    }

    private final class Impl implements Arena<T> {

        private T[] cache = (T[]) EMPTY_ARRAY;
        private int index;

        @Override
        public void push(@NotNull T value) {
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

        @Override
        public void close() {
            index = frames[--frameIndex];
        }
    }
}
