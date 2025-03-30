package io.karatelabs.js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsObjectTest extends EvalBase {

    @Test
    void testDev() {

    }

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
    void testConstructorThis() {
        eval("function Dog(name) { this.name = name }; var dog = new Dog('Fido'); var name = dog.name");
        assertEquals("Fido", get("name"));
    }

    @Test
    void testObjectApi() {
        matchEval("Object.keys({ a: 1, b: 2 })", "['a', 'b']");
        matchEval("Object.values({ a: 1, b: 2 })", "[1, 2]");
        matchEval("Object.entries({ a: 1, b: 2 })", "[['a', 1], ['b', 2]]");
        matchEval("Object.assign({}, { a: 1 }, { b: 2 })", "{ a: 1, b: 2 }");
        matchEval("Object.assign({ a: 0 }, { a: 1, b: 2 })", "{ a: 1, b: 2 }");
        matchEval("Object.fromEntries([['a', 1], ['b', 2]])", "{ a: 1, b: 2 }");
        matchEval("Object.fromEntries(Object.entries({ a: 1, b: 2 }))", "{ a: 1, b: 2 }");
        assertEquals(true, eval("Object.is(42, 42)"));
        assertEquals(true, eval("Object.is('foo', 'foo')"));
        assertEquals(false, eval("Object.is('foo', 'bar')"));
        assertEquals(false, eval("Object.is(null, undefined)"));
        assertEquals(true, eval("Object.is(null, null)"));
        assertEquals(true, eval("Object.is(NaN, NaN)"));
        // assertEquals(false, eval("Object.is(0, -0)"));
        matchEval("{}.valueOf()", "{}");
        matchEval("var obj = { a: 1, b: 2 }; obj.valueOf()", "{ a: 1, b: 2 }");
    }

    @Test
    void testObjectSpread() {
        matchEval("var obj1 = {a: 1, b: 2}; var obj2 = {...obj1}; obj2", "{ a: 1, b: 2 }");
        matchEval("var obj1 = {a: 1, b: 2}; var obj2 = {...obj1, b: 3}; obj2", "{ a: 1, b: 3 }");
        matchEval("var obj1 = {a: 1}; var obj2 = {b: 2}; var obj3 = {...obj1, ...obj2}; obj3", "{ a: 1, b: 2 }");
        matchEval("var obj1 = {a: 1, b: 2}; var obj2 = {b: 3, c: 4}; var obj3 = {...obj1, ...obj2}; obj3", "{ a: 1, b: 3, c: 4 }");
        matchEval("var obj1 = {a: 1, b: 2}; var obj2 = {b: 3, c: 4}; var obj3 = {...obj2, ...obj1}; obj3", "{ b: 2, c: 4, a: 1 }");
    }

}
