package io.karatelabs.js;

import io.karatelabs.js.test.JsonUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsArrayTest {

    @Test
    void testLengthAndMap() {
        List<Object> list = JsonUtils.fromJson("['foo', 'bar']");
        JsArray jl = new JsArray(list);
        assertEquals(2, jl.get("length"));
        Invokable invokable = (Invokable) jl.get("map");
        Invokable transform = (instance, args) -> args[0] + "bar";
        Object results = invokable.invoke(null, transform);
        assertEquals(List.of("foobar", "barbar"), results);
    }

    @Test
    void testFilter() {
        List<Object> list = JsonUtils.fromJson("[1, 2, 3, 4]");
        JsArray jl = new JsArray(list);
        Invokable invokable = (Invokable) jl.get("filter");
        Invokable transform = (instance, args) -> ((Integer) args[0]) % 2 == 0;
        Object results = invokable.invoke(null, transform);
        assertEquals(List.of(2, 4), results);
    }

}
