package io.karatelabs.js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsObjectTest extends EvalBase {

    @Test
    void testObject() {
        matchEval("{}", "{}");
        matchEval("{ a: 1 }", "{ a: 1 }");
        matchEval("{ a: 1, b: 2 }", "{ a: 1, b: 2 }");
        matchEval("{ 'a': 1 }", "{ a: 1 }");
        matchEval("{ \"a\": 1 }", "{ a: 1 }");
        matchEval("{ a: 'b' }", "{ a: 'b' }");
        matchEval("{ a: true }", "{ a: true }");
        matchEval("{ a: (1 + 2) }", "{ a: 3 }");
        matchEval("{ a: b }", "{ a: 5 }", "{ b: 5 }");
    }

    @Test
    void testObjectMutation() {
        assertEquals(3, eval("a.b = 1 + 2", "{ a: {} }"));
        NodeUtils.match(get("a"), "{ b: 3 }");
        eval("var a = { foo: 1, bar: 2 }; delete a.foo");
        NodeUtils.match(get("a"), "{ bar: 2 }");
        eval("var a = { foo: 1, bar: 2 }; delete a['bar']");
        NodeUtils.match(get("a"), "{ foo: 1 }");
    }

    @Test
    void testObjectProp() {
        assertEquals(2, eval("a = { b: 2 }; a.b"));
        assertEquals(2, eval("a = { b: 2 }; a['b']"));
    }

    @Test
    void testObjectPropReservedWords() {
        assertEquals(2, eval("a = { 'null': 2 }; a.null"));
    }

    @Test
    void testObjectFunction() {
        assertEquals("foo", eval("a = { b: function(){ return this.c }, c: 'foo' }; a.b()"));
    }

    @Test
    void testObjectEnhanced() {
        eval("a = 1; b = { a }");
        NodeUtils.match(get("b"), "{ a: 1 }");
    }

    @Test
    void testObjectPrototype() {
        String js = "function Dog(name){ this.name = name }; var dog = new Dog('foo');"
                + " Dog.prototype.toString = function(){ return this.name }; ";
        assertEquals("foo", eval(js + "dog.toString()"));
        assertEquals(true, eval(js + "dog.constructor === Dog"));
        assertEquals(true, eval(js + "dog instanceof Dog"));
    }

    @Test
    void testObjectApi() {
        match(eval("Object.keys({ a: 1, b: 2 })"), "['a', 'b']");
        match(eval("Object.values({ a: 1, b: 2 })"), "[1, 2]");
        match(eval("Object.entries({ a: 1, b: 2 })"), "[['a', 1], ['b', 2]]");
        match(eval("Object.assign({}, { a: 1 }, { b: 2 })"), "{ a: 1, b: 2 }");
        match(eval("Object.assign({ a: 0 }, { a: 1, b: 2 })"), "{ a: 1, b: 2 }");
        match(eval("Object.fromEntries([['a', 1], ['b', 2]])"), "{ a: 1, b: 2 }");
        match(eval("Object.fromEntries(Object.entries({ a: 1, b: 2 }))"), "{ a: 1, b: 2 }");
        assertEquals(true, eval("Object.is(42, 42)"));
        assertEquals(true, eval("Object.is('foo', 'foo')"));
        assertEquals(false, eval("Object.is('foo', 'bar')"));
        assertEquals(false, eval("Object.is(null, undefined)"));
        assertEquals(true, eval("Object.is(null, null)"));
        assertEquals(true, eval("Object.is(NaN, NaN)"));
        // assertEquals(false, eval("Object.is(0, -0)"));
    }

}
