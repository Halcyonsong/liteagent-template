package io.github.halcyonsong.liteagent.memory.window.impl;

import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryMemoryWindowStoreTest {

    @Test
    void shouldReturnSameWindowForSameSession() {
        InMemoryMemoryWindowStore store = new InMemoryMemoryWindowStore();

        MemoryWindow first = store.getOrCreate("session-1");
        MemoryWindow second = store.getOrCreate("session-1");

        assertSame(first, second);
        assertEquals("session-1", first.sessionId());
    }

    @Test
    void shouldKeepWindowsSeparatedBySession() {
        InMemoryMemoryWindowStore store = new InMemoryMemoryWindowStore();

        MemoryWindow first = store.getOrCreate("session-1");
        MemoryWindow second = store.getOrCreate("session-2");
        first.append(Messages.user("first"));

        assertNotSame(first, second);
        assertEquals(1, first.messages().size());
        assertEquals("first", first.messages().get(0).getContent());
        assertTrue(second.isEmpty());
    }

    @Test
    void shouldDeleteWindowAndCreateNewOneWhenRequestedAgain() {
        InMemoryMemoryWindowStore store = new InMemoryMemoryWindowStore();
        MemoryWindow original = store.getOrCreate("session-1");
        original.append(Messages.user("original"));

        store.delete("session-1");

        MemoryWindow recreated = store.getOrCreate("session-1");
        assertNotSame(original, recreated);
        assertTrue(recreated.isEmpty());
    }

    @Test
    void shouldIgnoreBlankSessionIdWhenDeleting() {
        InMemoryMemoryWindowStore store = new InMemoryMemoryWindowStore();
        MemoryWindow window = store.getOrCreate("session-1");

        store.delete(null);
        store.delete(" ");

        assertSame(window, store.getOrCreate("session-1"));
    }

    @Test
    void shouldLoadHistoryOnlyWhenWindowIsFirstCreated() {
        TrackingMemoryWindowStore store = new TrackingMemoryWindowStore();

        MemoryWindow first = store.getOrCreate("session-1");
        MemoryWindow second = store.getOrCreate("session-1");

        assertSame(first, second);
        assertEquals(1, store.loadCount);
        assertEquals(1, first.messages().size());
        assertEquals("history", first.messages().get(0).getContent());
    }

    @Test
    void shouldRejectBlankSessionIdWhenCreatingWindow() {
        InMemoryMemoryWindowStore store = new InMemoryMemoryWindowStore();

        assertThrows(IllegalArgumentException.class, () -> store.getOrCreate(null));
        assertThrows(IllegalArgumentException.class, () -> store.getOrCreate(" "));
    }

    private static final class TrackingMemoryWindowStore
            extends InMemoryMemoryWindowStore {

        private int loadCount;

        @Override
        public List<io.github.halcyonsong.liteagent.core.message.norm.Message> loadHistory(
                String sessionId
        ) {
            loadCount++;
            return List.of(Messages.user("history"));
        }
    }
}
