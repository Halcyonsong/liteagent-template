package io.github.halcyonsong.liteagent.memory.window.impl;

import io.github.halcyonsong.liteagent.core.message.norm.Message;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryMemoryWindowTest {

    @Test
    void shouldKeepSessionIdAndStartEmpty() {
        InMemoryMemoryWindow window = new InMemoryMemoryWindow("session-1");

        assertEquals("session-1", window.sessionId());
        assertTrue(window.isEmpty());
        assertEquals(0, window.size());
        assertTrue(window.peekEarliest().isEmpty());
        assertTrue(window.peekLatest().isEmpty());
    }

    @Test
    void shouldAppendAndReadMessagesInFifoOrder() {
        InMemoryMemoryWindow window = new InMemoryMemoryWindow("session-1");
        Message first = Messages.user("first");
        Message second = Messages.assistant("second");
        Message third = Messages.user("third");

        window.append(first);
        window.appendAll(List.of(second, third));

        assertEquals(3, window.size());
        assertEquals(first, window.peekEarliest().orElseThrow());
        assertEquals(third, window.peekLatest().orElseThrow());
        assertEquals(List.of(first, second, third), window.messages());
    }

    @Test
    void shouldSupportBothEndRemovalOperations() {
        InMemoryMemoryWindow window = new InMemoryMemoryWindow("session-1");
        Message first = Messages.user("first");
        Message second = Messages.assistant("second");
        Message third = Messages.user("third");
        window.appendAll(List.of(first, second, third));

        assertEquals(first, window.pollEarliest().orElseThrow());
        assertEquals(third, window.pollLatest().orElseThrow());
        assertEquals(List.of(second), window.messages());

        window.removeEarliest();
        assertTrue(window.isEmpty());

        window.removeEarliest();
        window.removeLatest();
        assertTrue(window.isEmpty());
    }

    @Test
    void shouldReturnReadonlySnapshotWithoutChangingWindow() {
        InMemoryMemoryWindow window = new InMemoryMemoryWindow("session-1");
        Message message = Messages.user("hello");
        window.append(message);

        List<Message> snapshot = window.messages();

        assertEquals(List.of(message), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(Messages.assistant("reply")));
        assertEquals(List.of(message), window.messages());
        assertEquals(1, window.size());
    }

    @Test
    void shouldClearAllMessages() {
        InMemoryMemoryWindow window = new InMemoryMemoryWindow("session-1");
        window.appendAll(List.of(Messages.user("hello"), Messages.assistant("world")));

        window.clear();

        assertTrue(window.isEmpty());
        assertEquals(0, window.size());
        assertEquals(List.of(), window.messages());
        assertTrue(window.pollEarliest().isEmpty());
        assertTrue(window.pollLatest().isEmpty());
    }

    @Test
    void shouldRejectBlankSessionIdAndNullMessages() {
        assertThrows(IllegalArgumentException.class, () -> new InMemoryMemoryWindow(null));
        assertThrows(IllegalArgumentException.class, () -> new InMemoryMemoryWindow(" "));

        InMemoryMemoryWindow window = new InMemoryMemoryWindow("session-1");
        assertThrows(NullPointerException.class, () -> window.append(null));
        assertThrows(NullPointerException.class, () -> window.appendAll(null));
        assertTrue(window.isEmpty());
    }
}
