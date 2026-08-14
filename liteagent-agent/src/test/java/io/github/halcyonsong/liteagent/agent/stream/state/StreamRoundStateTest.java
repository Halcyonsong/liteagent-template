package io.github.halcyonsong.liteagent.agent.stream.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StreamRoundStateTest {

    @Test
    void constructor_should_set_round_index() {
        StreamRoundState state = new StreamRoundState(0);
        assertEquals(0, state.getRoundIndex());
    }

    @Test
    void constructor_should_reject_negative_index() {
        assertThrows(IllegalArgumentException.class, () -> new StreamRoundState(-1));
    }

    @Test
    void default_flags_should_be_false() {
        StreamRoundState state = new StreamRoundState(0);
        assertFalse(state.isRoundComplete());
    }

    @Test
    void set_round_complete_and_next_round_should_work() {
        StreamRoundState state = new StreamRoundState(0);
        state.setRoundComplete(true);
        assertTrue(state.isRoundComplete());
    }

    @Test
    void accumulator_should_store_and_retrieve_object() {
        StreamRoundState state = new StreamRoundState(0);
        Object acc = new Object();
        state.setAccumulator(acc);
        assertSame(acc, state.getAccumulator());
    }

    @Test
    void final_response_should_store_and_retrieve_object() {
        StreamRoundState state = new StreamRoundState(0);
        Object response = new Object();
        state.setFinalResponse(response);
        assertSame(response, state.getFinalResponse());
    }

    @Test
    void set_attribute_should_put_replace_and_remove() {
        StreamRoundState state = new StreamRoundState(0);
        state.setAttribute("k1", "v1");
        assertEquals("v1", state.getAttribute("k1"));
        assertEquals("v1", state.getAttribute("k1", String.class));

        state.setAttribute("k1", "v2");
        assertEquals("v2", state.getAttribute("k1"));

        state.setAttribute("k1", null);
        assertNull(state.getAttribute("k1"));
    }

    @Test
    void set_attribute_should_reject_blank_key() {
        StreamRoundState state = new StreamRoundState(0);
        assertThrows(IllegalArgumentException.class, () -> state.setAttribute(null, "v"));
        assertThrows(IllegalArgumentException.class, () -> state.setAttribute(" ", "v"));
    }
}
