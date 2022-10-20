package dev.xdark.jlinker;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArenaTest {

    @MethodSource("allocators")
    @ParameterizedTest
    public void testPush(ArenaAllocator<String> allocator) {
        try (Arena<String> arena = allocator.push()) {
            arena.push("1");
            arena.push("2");
            arena.push("3");
            assertEquals("3", arena.poll());
            assertEquals("2", arena.poll());
            assertEquals("1", arena.poll());
            assertNull(arena.poll());
        }
    }

    @MethodSource("allocators")
    @ParameterizedTest
    public void testPushNested(ArenaAllocator<String> allocator) {
        try (Arena<String> arena1 = allocator.push()) {
            arena1.push("1");
            try (Arena<String> arena2 = allocator.push()) {
                arena2.push("2");
                assertEquals("2", arena2.poll());
            }
            assertEquals("1", arena1.poll());
            assertNull(arena1.poll());
        }
    }

    private static List<ArenaAllocator<String>> allocators() {
        return Arrays.asList(new FrameArenaAllocator<>());
    }
}
