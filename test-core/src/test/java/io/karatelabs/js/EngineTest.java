package io.karatelabs.js;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EngineTest {

    static final Logger logger = LoggerFactory.getLogger(EngineTest.class);

    @Test
    void test01() {
        File file = new File("src/test/resources/test-01.js");
        Engine engine = new Engine();
        Object result = engine.eval(file);
        assertEquals("foobar", result);
        assertEquals("foo", engine.context.get("foo"));
    }

    @Test
    void test02() {
        File file = new File("src/test/resources/test-02.js");
        Engine engine = new Engine();
        Object result = engine.eval(file);
        assertEquals(Map.of("data", "{\\\"myKey\\\":\\\"myValue\\\"}"), result);
    }

    @Test
    void testUndefined() {
        Engine engine = new Engine();
        Object result = engine.eval("1 * 'a'");
        assertTrue(Engine.isUndefined(result));
        result = engine.eval("foo.bar");
        assertTrue(Engine.isUndefined(result));
    }

    @Test
    void testErrorLog() {
        Engine engine = new Engine();
        try {
            engine.eval("var a = 1;\nvar b = a();");
            fail("expected error");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("cannot find method [a]"));
        }
    }

}
