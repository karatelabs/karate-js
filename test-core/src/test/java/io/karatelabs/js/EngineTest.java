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
    void test03() {
        File file = new File("src/test/resources/test-03.js");
        Source source = Source.of(file);
        Parser parser = new Parser(source);
        Node node = parser.parse();
        Node lastLine = node.findFirst(Token.CONST);
        assertEquals(10, lastLine.chunk.line);
    }

    @Test
    void test04() {
        File file = new File("src/test/resources/test-04.js");
        Source source = Source.of(file);
        Parser parser = new Parser(source);
        Node node = parser.parse();
        assertEquals(2, node.children.size());
        for (Node child : node.children) {
            assertEquals(Type.STATEMENT, child.type);
        }
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
