package io.karatelabs.js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaClassTest {

    @Test
    void testConstruct() {
        JavaClass proxy = new JavaClass("java.util.Properties");
        Object o = proxy.construct(JavaBridge.EMPTY);
        assertEquals("java.util.Properties", o.getClass().getName());
    }


}
