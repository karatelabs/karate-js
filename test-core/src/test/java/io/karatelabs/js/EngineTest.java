package io.karatelabs.js;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void testUndefined() {
        Engine engine = new Engine();
        Object result = engine.eval("1 * 'a'");
        assertTrue(Engine.isUndefined(result));
        result = engine.eval("foo.bar");
        assertTrue(Engine.isUndefined(result));
    }

}
