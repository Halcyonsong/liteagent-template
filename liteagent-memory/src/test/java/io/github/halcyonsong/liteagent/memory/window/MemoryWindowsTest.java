package io.github.halcyonsong.liteagent.memory.window;

import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class MemoryWindowsTest {

    @Test
    void shouldCreateIndependentInMemoryStores() {
        MemoryWindowStore first = MemoryWindows.inMemory();
        MemoryWindowStore second = MemoryWindows.inMemory();

        assertNotSame(first, second);
    }

    @Test
    void shouldReturnSharedStoreInstance() {
        MemoryWindowStore first = MemoryWindows.shared();
        MemoryWindowStore second = MemoryWindows.shared();

        assertSame(first, second);
    }
}
