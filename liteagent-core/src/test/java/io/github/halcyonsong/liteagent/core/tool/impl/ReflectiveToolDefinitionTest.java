package io.github.halcyonsong.liteagent.core.tool.impl;

import io.github.halcyonsong.liteagent.core.tool.norm.ExecutableToolDefinition;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReflectiveToolDefinitionTest {

    @Test
    void should_create_with_all_fields() throws NoSuchMethodException {
        Object target = new TestTool();
        Method method = TestTool.class.getDeclaredMethod("doSomething", String.class);

        ReflectiveToolDefinition def = new ReflectiveToolDefinition(
                "do_something",
                "does a thing",
                Map.of("type", "object"),
                target,
                method
        );

        assertEquals("do_something", def.getName());
        assertEquals("does a thing", def.getDescription());
        assertEquals(Map.of("type", "object"), def.getParameters());
        assertSame(target, def.getTarget());
        assertSame(method, def.getMethod());
    }

    @Test
    void should_normalize_null_description_to_empty() throws NoSuchMethodException {
        ReflectiveToolDefinition def = new ReflectiveToolDefinition(
                "test",
                null,
                Map.of(),
                new TestTool(),
                TestTool.class.getDeclaredMethod("doSomething", String.class)
        );

        assertEquals("", def.getDescription());
    }

    @Test
    void should_implement_executable_tool_definition() throws NoSuchMethodException {
        ReflectiveToolDefinition def = new ReflectiveToolDefinition(
                "test", "desc", Map.of(),
                new TestTool(),
                TestTool.class.getDeclaredMethod("doSomething", String.class)
        );

        assertInstanceOf(ExecutableToolDefinition.class, def);
    }

    @Test
    void should_reject_null_name() throws NoSuchMethodException {
        assertThrows(NullPointerException.class, () -> new ReflectiveToolDefinition(
                null, "desc", Map.of(),
                new TestTool(),
                TestTool.class.getDeclaredMethod("doSomething", String.class)
        ));
    }

    @Test
    void should_reject_null_parameters() throws NoSuchMethodException {
        assertThrows(NullPointerException.class, () -> new ReflectiveToolDefinition(
                "test", "desc", null,
                new TestTool(),
                TestTool.class.getDeclaredMethod("doSomething", String.class)
        ));
    }

    @Test
    void should_reject_null_target() throws NoSuchMethodException {
        assertThrows(NullPointerException.class, () -> new ReflectiveToolDefinition(
                "test", "desc", Map.of(),
                null,
                TestTool.class.getDeclaredMethod("doSomething", String.class)
        ));
    }

    @Test
    void should_reject_null_method() {
        assertThrows(NullPointerException.class, () -> new ReflectiveToolDefinition(
                "test", "desc", Map.of(),
                new TestTool(),
                null
        ));
    }

    public static class TestTool {
        public String doSomething(String input) {
            return input;
        }
    }
}
