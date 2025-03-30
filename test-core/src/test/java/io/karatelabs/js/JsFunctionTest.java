package io.karatelabs.js;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JsFunctionTest extends EvalBase {

    @Test
    void testDev() {

    }

    @Test
    void testFunction() {
        assertEquals(true, eval("var a = function(){ return true }; a()"));
        assertEquals(2, eval("var a = 2; var b = function(){ return a }; b()"));
        assertEquals(5, eval("var fn = function(x, y){ return x + y }; fn(2, 3)"));
        assertEquals(5, eval("function add(x, y){ return x + y }; add(2, 3)"));
    }

    @Test
    void testFunctionNested() {
        assertEquals(true, eval("var a = {}; a.b = function(){ return true }; a.b()"));
        assertEquals(true, eval("var a = {}; a.b = function(){ return true }; a['b']()"));
        assertEquals(2, eval("var a = function(){}; a.b = [1, 2, 3]; a['b'][1]"));
    }

    @Test
    void testArrowFunction() {
        assertEquals(true, eval("var a = () => true; a()"));
        assertEquals(true, eval("var a = x => true; a()"));
        assertEquals(2, eval("var a = x => x; a(2)"));
        assertEquals(2, eval("var a = (x) => x; a(2)"));
        assertEquals(5, eval("var fn = (x, y) => x + y; fn(2, 3)"));
    }

    @Test
    void testFunctionBlocksAndReturn() {
        assertNull(eval("var a = function(){ }; a()"));
        assertEquals(true, eval("var a = function(){ return true; 'foo' }; a()"));
        assertEquals("foo", eval("var a = function(){ if (true) return 'foo'; return 'bar' }; a()"));
        assertEquals("foo", eval("var a = function(){ for (var i = 0; i < 2; i++) { return 'foo' }; return 'bar' }; a()"));
        assertNull(eval("var a = () => {}; a()"));
        assertNull(eval("var a = () => { true }; a()"));
        assertEquals(true, eval("var a = () => { return true }; a()"));
        assertEquals(true, eval("var a = () => { return true; 'foo' }; a()"));
    }

    @Test
    void testFunctionThis() {
        assertEquals("bar", eval("var a = function(){ return this.foo }; a.foo = 'bar'; a()"));
        eval("function a(b){ try { b() } catch (e) { this.c = e.message } }; a(() => { throw new Error('foo') })");
        JsFunction fun = (JsFunction) get("a");
        assertEquals("foo", fun.get("c"));
    }

    @Test
    void testFunctionArgsMissing() {
        assertEquals(true, eval("var a = function(b){ return b }; a() === undefined"));
    }

    @Test
    void testFunctionNew() {
        assertEquals("foo", eval("var a = function(x){ this.b = x }; c = new a('foo'); c.b"));
    }

    @Test
    void testFunctionArguments() {
        assertEquals(List.of(1, 2), eval("var a = function(){ return arguments }; a(1, 2)"));
    }

    @Test
    void testFunctionCallSpread() {
        assertEquals(List.of(1, 2), eval("var a = function(){ return arguments }; var b = [1, 2]; a(...b)"));
    }

    @Test
    void testFunctionPrototypeToString() {
        assertEquals("[object Object]", eval("var a = function(){ }; a.toString()"));
        assertEquals("a", eval("var a = function(){ }; a.constructor.name"));
        assertEquals("a", eval("function a(){ }; a.constructor.name"));
        assertEquals("foo", eval("var a = function(){ }; a.prototype.toString = function(){ return 'foo' }; a.toString()"));
    }

    @Test
    void testFunctionDeclarationRest() {
        assertEquals(List.of(1, 2, 3), eval("function sum(...args) { return args }; sum(1, 2, 3)"));
        assertEquals(6, eval("function sum(...numbers) { return numbers.reduce((a, b) => a + b, 0) }; sum(1,2,3)"));
        assertEquals("hello world", eval("function concat(first, ...rest) { return first + ' ' + rest.join(' ') }; concat('hello', 'world')"));
        assertEquals("hello world and more", eval("function concat(first, ...rest) { return first + ' ' + rest.join(' ') }; concat('hello', 'world', 'and', 'more')"));
    }

    @Test
    void testArrowFunctionRest() {
        assertEquals("[1,2,3]", eval("var sum = (...args) => args; JSON.stringify(sum(1,2,3))"));
        assertEquals(6, eval("var sum = (...numbers) => numbers.reduce((a, b) => a + b, 0); sum(1,2,3)"));
        assertEquals("hello world", eval("var concat = (first, ...rest) => first + ' ' + rest.join(' '); concat('hello', 'world')"));
    }

    @Test
    void testCurrying() {
        matchEval("function multiply(a) { return function(b) { return a * b } }; multiply(4)(7)", "28");
    }

    @Test
    void testIife() {
        matchEval("(function(){ return 'hello' })()", "'hello'");
    }

}
