package io.karatelabs.js;

import io.karatelabs.js.test.JsonUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JavaInteropTest extends EvalBase {

    @Test
    void testArrayLengthAndMap() {
        List<Object> list = JsonUtils.fromJson("['foo', 'bar']");
        JsArray jl = new JsArray(list);
        assertEquals(2, jl.get("length"));
        Invokable invokable = (Invokable) jl.get("map");
        Invokable transform = args -> args[0] + "bar";
    }

    @Test
    void testBytes() {
        assertEquals(3, eval("var a = 'foo'.getBytes(); a.length"));
    }

    @Test
    void testJavaInterop() {
        eval("var DemoUtils = Java.type('io.karatelabs.js.DemoUtils'); var b = DemoUtils.doWork()");
        assertEquals("hello", get("b"));
        eval("var DemoUtils = Java.type('io.karatelabs.js.DemoUtils'); var b = DemoUtils.doWork; var c = b()");
        assertEquals("hello", get("c"));
        eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); var b = new DemoPojo(); var c = b.doWork()");
        assertEquals("hello", get("c"));
        eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); var b = new DemoPojo(); b.stringValue = 'foo'; var c = b.stringValue");
        assertEquals("foo", get("c"));
        eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); var b = new DemoPojo('foo'); var c = b.stringValue");
        assertEquals("foo", get("c"));
        eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); var b = new DemoPojo('foo', 42); var c = b.stringValue; var d = b.intValue");
        assertEquals("foo", get("c"));
        assertEquals(42, get("d"));
        eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); var b = new DemoPojo(); var c = b.stringValue");
        assertNull(get("c"));
        eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); var b = new DemoPojo('foo'); b.integerArray = [1, 2]; var c = b.integerArray; var d = b.integerArray[1]");
        NodeUtils.match(get("c"), "[1, 2]");
        assertEquals(2, get("d"));
        eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); var b = new DemoPojo('foo'); b.intArray = [1, 2]; var c = b.intArray; var d = b.intArray[1]");
        NodeUtils.match(get("c"), "[1, 2]");
        assertEquals(2, get("d"));
        assertEquals("static-field", eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); DemoPojo.staticField"));
        assertEquals("static-field-changed", eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); DemoPojo.staticField = 'static-field-changed'; DemoPojo.staticField"));
        assertEquals("foo", eval("io.karatelabs.js.DemoPojo.staticField = 'foo'; var a = io.karatelabs.js.DemoPojo.staticField"));
        assertEquals("foo", get("a"));
        assertEquals("instance-field", eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); var a = new DemoPojo(); a.instanceField"));
        eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); var b = new DemoPojo(); var c = b.doWork; var d = c()");
        assertEquals("hello", get("d"));
        eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); var b = new DemoPojo(); var c = b.doWorkFn(); var d = c(2)");
        assertEquals("2", get("d"));
        eval("var DemoPojo = Java.type('io.karatelabs.js.DemoPojo'); var b = new DemoPojo('foo'); b.integerArray = [1, 2]; var c = b.doIntegerArray()");
        NodeUtils.match(get("c"), "[1, 2]");
    }

    @Test
    void testJavaInteropJdk() {
        assertEquals("bar", eval("var props = new java.util.Properties(); props.put('foo', 'bar'); props.get('foo')"));
        assertEquals(new BigDecimal(123123123123L), eval("new java.math.BigDecimal(123123123123)"));
        assertEquals(String.CASE_INSENSITIVE_ORDER, eval("java.lang.String.CASE_INSENSITIVE_ORDER"));
        assertEquals("aGVsbG8=", eval("var Base64 = Java.type('java.util.Base64'); Base64.getEncoder().encodeToString('hello'.getBytes())"));
        assertInstanceOf(UUID.class, eval("java.util.UUID.randomUUID()"));
    }

}
