package io.karatelabs.js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaObjectTest {

    @Test
    void testCall() {
        JavaClass cp = new JavaClass("java.util.Properties");
        Object o = cp.construct(JavaBridge.EMPTY);
        JavaObject op = new JavaObject(o);
        assertEquals(0, op.call("size", JavaBridge.EMPTY));
        op.call("put", "foo", 5);
        assertEquals(5, op.call("get", "foo"));
    }

    @Test
    void testGet() {
        DemoPojo dp = new DemoPojo();
        dp.setStringValue("foo");
        dp.setIntValue(5);
        dp.setBooleanValue(true);
        JavaObject jo = new JavaObject(dp);
        assertEquals("foo", jo.get("stringValue"));
        assertEquals(5, jo.get("intValue"));
        assertEquals(true, jo.get("booleanValue"));
    }

    @Test
    void testSet() {
        DemoPojo dp = new DemoPojo();
        JavaObject jo = new JavaObject(dp);
        jo.put("stringValue", "bar");
        jo.put("intValue", 10);
        jo.put("booleanValue", true);
        assertEquals("bar", dp.getStringValue());
        assertEquals(10, dp.getIntValue());
        assertTrue(dp.isBooleanValue());
    }

    @Test
    void testSetSpecial() {
        DemoPojo dp = new DemoPojo();
        JavaObject jo = new JavaObject(dp);
        jo.put("doubleValue", 10);
        jo.put("booleanValue", Boolean.TRUE);
        assertEquals(10, dp.getDoubleValue());
        assertTrue(dp.isBooleanValue());
    }

    @Test
    void testVarArgs() {
        DemoPojo dp = new DemoPojo();
        JavaObject jo = new JavaObject(dp);
        JavaInvokable method = new JavaInvokable("varArgs", jo);
        assertEquals("foo", method.invoke(null, "foo"));
        assertEquals("bar", method.invoke(null, "foo", "bar"));
    }

    @Test
    void testMethodOverload() {
        DemoPojo dp = new DemoPojo();
        JavaObject jo = new JavaObject(dp);
        JavaInvokable method = new JavaInvokable("doWork", jo);
        assertEquals("hello", method.invoke());
        assertEquals("hellofoo", method.invoke("foo"));
        assertEquals("hellofootrue", method.invoke("foo", true));
    }

}
