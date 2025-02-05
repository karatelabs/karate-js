package io.karatelabs.js;

import io.karatelabs.js.test.JsonUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EvalTest {

    static final Logger logger = LoggerFactory.getLogger(EvalTest.class);

    Context context;

    Object eval(String text) {
        return eval(text, null);
    }

    private Object eval(String text, String vars) {
        Parser parser = new Parser(Source.of(text));
        Node node = parser.parse();
        context = Context.root();
        if (vars != null) {
            Map<String, Object> map = JsonUtils.fromJson(vars);
            map.forEach((k, v) -> context.declare(k, v));
        }
        return Interpreter.eval(node, context);
    }

    private void matchEval(String text, String expected) {
        matchEval(text, expected, null);
    }

    private void matchEval(String text, String expected, String vars) {
        match(eval(text, vars), expected);
    }

    private void match(Object actual, String expected) {
        NodeUtils.match(actual, expected);
    }

    private Object get(String varName) {
        return context.get(varName);
    }

    @Test
    void testDev() {

    }

    @Test
    void testNumbers() {
        assertEquals(1, eval("1"));
        assertEquals(1, eval("1.0"));
        assertEquals(0.5d, eval(".5"));
        assertEquals(65280, eval("0x00FF00"));
        assertEquals(2000001813346120L, eval("2.00000181334612E15"));
    }

    @Test
    void testPost() {
        assertEquals(1, eval("a = 1; a++"));
        assertEquals(2, get("a"));
        assertEquals(1, eval("a = 1; a--"));
        assertEquals(0, get("a"));
        assertEquals(1, eval("a = { b: 1 }; a.b++"));
        NodeUtils.match(get("a"), "{ b: 2 }");
    }

    @Test
    void testPre() {
        assertEquals(2, eval("a = 1; ++a"));
        assertEquals(2, get("a"));
        assertEquals(0, eval("a = 1; --a"));
        assertEquals(0, get("a"));
        assertEquals(-5, eval("a = 5; -a"));
        assertEquals(5, get("a"));
        assertEquals(2, eval("a = 2; +a"));
        assertEquals(2, get("a"));
        assertEquals(3, eval("a = '3'; +a"));
        assertEquals("3", get("a"));
        assertEquals(2, eval("a = { b: 1 }; ++a.b"));
        NodeUtils.match(get("a"), "{ b: 2 }");
    }

    @Test
    void testLiterals() {
        assertNull(eval("null"));
        assertEquals(true, eval("true"));
        assertEquals(false, eval("false"));
        assertEquals("foo", eval("'foo'"));
        assertEquals("bar", eval("\"bar\""));
    }

    @Test
    void testRegex() {
        try {
            eval("(/abc/)");
            fail("regex should fail");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("regexes not supported"));
        }
    }

    @Test
    void testExprList() {
        assertEquals(3, eval("1, 2, 3"));
    }

    @Test
    void testAssign() {
        assertEquals(1, eval("a = 1"));
        assertEquals(1, get("a"));
        assertEquals(2, eval("a = 2; b = a"));
        assertEquals(2, get("b"));
        assertEquals(3, eval("a = 1 + 2"));
        assertEquals(3, get("a"));
        assertEquals(2, eval("a = 1; a += 1"));
        assertEquals(2, get("a"));
        assertEquals(2, eval("a = 3; a -= 1"));
        assertEquals(2, get("a"));
        assertEquals(6, eval("a = 2; a *= 3"));
        assertEquals(6, get("a"));
        assertEquals(3, eval("a = 6; a /= 2"));
        assertEquals(3, get("a"));
        assertEquals(1, eval("a = 3; a %= 2"));
        assertEquals(1, get("a"));
        assertEquals(9, eval("a = 3; a **= 2"));
        assertEquals(9, get("a"));
        eval("var a = { foo: 'bar' }");
        match(get("a"), "{ foo: 'bar' }");
        eval("var a = { 0: 'a', 1: 'b' }");
        match(get("a"), "{ '0': 'a', '1': 'b' }");
    }

    @Test
    void testVarStatement() {
        assertEquals(Undefined.INSTANCE, eval("var a"));
        assertEquals(Undefined.INSTANCE, get("a"));
        assertEquals(1, eval("var a = 1"));
        assertEquals(1, get("a"));
        assertEquals(2, eval("var a, b = 2"));
        assertEquals(2, get("a"));
        assertEquals(2, get("b"));
    }

    @Test
    void testMath() {
        assertEquals(3, eval("1 + 2"));
        assertEquals(1, eval("3 - 2"));
        assertEquals(6, eval("1 + 2 + 3"));
        assertEquals(0, eval("1 + 2 - 3"));
        assertEquals(1.5d, eval("1 + 0.5"));
        assertEquals(6, eval("3 * 2"));
        assertEquals(3, eval("6 / 2"));
        assertEquals(1.5d, eval("3 / 2"));
        assertEquals(0, eval("8 % 2"));
        assertEquals(2, eval("11 % 3"));
        assertEquals(7, eval("1 + 3 * 2"));
        assertEquals(8, eval("2 * 3 + 2"));
        assertEquals(8, eval("(1 + 3) * 2"));
        assertEquals(8, eval("2 * (1 + 3)"));
        assertEquals(8, eval("2 ** 3"));
    }

    @Test
    void testMathSpecial() {
        assertEquals(Terms.POSITIVE_INFINITY, eval("5 / 0"));
        assertEquals(0, eval("5 / Infinity"));
    }

    @Test
    void testExp() {
        assertEquals(512, eval("2 ** 3 ** 2"));
        assertEquals(64, eval("(2 ** 3) ** 2"));
    }

    @Test
    void testBitwise() {
        assertEquals(3, eval("1 | 2"));
        assertEquals(7, eval("3 | 2 | 4"));
        assertEquals(20, eval("5 << 2"));
        assertEquals(4294967295L, eval("var a = -1; a >>>= 0"));
        assertEquals(1, eval("a = 5; a >>= 2"));
        assertEquals(1073741822, eval("a = -5; a >>>= 2"));
    }

    @Test
    void testLogic() {
        assertEquals(true, eval("2 > 1"));
        assertEquals(2, eval("2 || 1"));
        assertEquals("b", eval("'a' && 'b'"));
        assertEquals(true, eval("2 > 1 && 3 < 5"));
        assertEquals(true, eval("2 == '2'"));
        assertEquals(false, eval("2 === '2'"));
    }

    @Test
    void testLogicNonNumbers() {
        assertEquals(false, eval("'a' == 'b'"));
        assertEquals(false, eval("'a' === 'b'"));
        assertEquals(true, eval("'a' == 'a'"));
        assertEquals(true, eval("'a' === 'a'"));
        assertEquals(true, eval("'a' != 'b'"));
        assertEquals(true, eval("'a' !== 'b'"));
        assertEquals(false, eval("'a' != 'a'"));
        assertEquals(false, eval("'a' !== 'a'"));
    }

    @Test
    void testLogicSpecial() {
        assertEquals(false, eval("a = undefined; b = 0; a < b"));
        assertEquals(true, eval("a = ''; b = ''; a == b"));
        assertEquals(false, eval("a = 0; b = -0; a == b"));
        assertEquals(true, eval("a = 0; b = -0; a === b"));
        assertEquals(false, eval("a = 0; b = -0; 1 / a === 1 / b"));
        assertEquals(true, eval("a = Infinity; 1 / a === 0"));
        assertEquals(true, eval("a = -Infinity; 1 / a === -0"));
        assertEquals(true, eval("a = 0; 1 / a === Infinity"));
        assertEquals(true, eval("a = 0; -1 / a === -Infinity"));
        assertEquals(false, eval("a = {}; b = {}; a === b"));
        assertEquals(false, eval("a = []; b = []; a === b"));
        assertEquals(false, eval("a = {}; b = {}; a !== a && b !== b"));
        assertEquals(false, eval("a = []; b = []; a !== a && b !== b"));
        assertEquals(false, eval("!!null"));
        assertEquals(false, eval("!!NaN"));
        assertEquals(false, eval("!!undefined"));
        assertEquals(false, eval("!!''"));
        assertEquals(false, eval("!!0"));
        assertEquals(true, eval("a = null; b = undefined; a == b"));
        assertEquals(false, eval("a = null; b = undefined; a === b"));
        assertEquals(true, eval("a = undefined; b = null; a == b"));
        assertEquals(false, eval("a = undefined; b = null; a === b"));
        assertEquals(true, eval("a = null; b = null; a == b"));
        assertEquals(true, eval("a = null; b = null; a === b"));
        assertEquals(true, eval("a = undefined; b = undefined; a == b"));
        assertEquals(true, eval("a = undefined; b = undefined; a === b"));
        assertEquals(true, eval("a = null; b = null; a == b"));
        assertEquals(true, eval("a = null; b = undefined; a == b"));
        assertEquals(false, eval("a = NaN; b = NaN; a == b"));
        assertEquals(false, eval("a = NaN; b = NaN; a === b"));
        assertEquals(true, eval("a = NaN; b = NaN; a != b"));
        assertEquals(true, eval("a = NaN; b = NaN; a !== b"));
        assertEquals(true, eval("a = NaN; b = NaN; a !== a && b !== b"));
        assertEquals(false, eval("a = NaN; b = NaN; a < b"));
        assertEquals(false, eval("a = NaN; b = NaN; a > b"));
        assertEquals(false, eval("a = NaN; b = NaN; a <= b"));
        assertEquals(false, eval("a = NaN; b = NaN; a >= b"));
    }

    @Test
    void testUnary() {
        assertEquals(true, eval("!false"));
        assertEquals(-6, eval("~5"));
        assertEquals(2, eval("~~(2.7)"));
    }

    @Test
    void testStringConcat() {
        assertEquals("foobar", eval("'foo' + 'bar'"));
        assertEquals("abc", eval("'a' + 'b' + 'c'"));
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
    void testStringWithEscapes() {
        assertEquals("foo\nbar", eval("'foo\nbar'"));
        assertEquals("foo\nbar", eval("\"foo\nbar\""));
        assertEquals("foo\nbarxxxbaz", eval("var a = 'xxx'; 'foo\nbar' + a + 'baz'"));
        assertEquals("fooxxxbar", eval("'foo\nbar'.replaceAll('\n', 'xxx')"));
        assertEquals("fooxxxbar", eval("'foo\nbar'.replaceAll(\"\n\", 'xxx')"));
    }

    @Test
    void testStringConcatExpr() {
        assertEquals("foobar", eval("var a = function(){ return 'bar' }; 'foo' + a()"));
        assertEquals("foobar", eval("var a = ['bar']; b = 'foo' + a[0]; b"));
    }

    @Test
    void testStringAsArray() {
        assertEquals("o", eval("var a = 'foo'; a[1]"));
    }

    @Test
    void testStringTemplate() {
        assertEquals("foobar", eval("var a = 'foo'; `${a}bar`"));
        assertEquals("foobar", eval("var a = x => 'foo'; `${a()}bar`"));
        assertEquals("[1, 2, 3]", eval("`[${[].map.call([1,2,3], String).join(', ')}]`"));
    }

    @Test
    void testStringTemplateNested() {
        assertEquals("foofoo", eval("var name = 'foo'; `${ name + `${name}` }`"));
        assertEquals("[ xxfooxx ]", eval("var name = 'foo'; `[ ${name ? `xx${name}xx` : ''} ]`"));
    }

    @Test
    void testArray() {
        matchEval("[]", "[]");
        matchEval("[ 1 ]", "[ 1 ]");
        matchEval("[ 1, 2, 3 ]", "[ 1, 2, 3 ]");
        matchEval("[ true ]", "[ true ]");
        matchEval("[ 'a' ]", "[ 'a' ]");
        matchEval("[ \"a\" ]", "[ 'a' ]");
        matchEval("[ (1 + 2) ]", "[ 3 ]");
        matchEval("[ a ]", "[ 5 ]", "{ a: 5 }");
    }

    @Test
    void testArraySparse() {
        matchEval("[,]", "[null]");
    }

    @Test
    void testArrayProp() {
        assertEquals(2, eval("a = [1, 2]; a[1]"));
        assertEquals("bar", eval("a = ['bar']; a[0]"));
    }

    @Test
    void testArrayMutation() {
        eval("var a = [1, 2]; a[1] = 3");
        match(get("a"), "[1, 3]");
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
    void testObjectPrototype() {
        String js = "function Dog(name){ this.name = name }; var dog = new Dog('foo');"
                + " Dog.prototype.toString = function(){ return this.name }; ";
        assertEquals("foo", eval(js + "dog.toString()"));
        assertEquals(true, eval(js + "dog.constructor === Dog"));
        assertEquals(true, eval(js + "dog instanceof Dog"));
    }

    @Test
    void testStringApi() {
        assertEquals(3, eval("a = 'foobar'; a.indexOf('bar')"));
        assertEquals(3, eval("a = 'foo'; a.length"));
        assertEquals(true, eval("a = 'foobar'; a.startsWith('foo')"));
        assertEquals("FOObar", eval("a = 'foobar'; a.replaceAll('foo', 'FOO')"));
        // this is non-standard but useful for java interop
        eval("var a = 'foo'; var b = a.getBytes()");
        byte[] bytes = (byte[]) get("b");
        assertEquals(3, bytes.length);
        assertEquals('f', bytes[0]);
        assertEquals('o', bytes[1]);
        assertEquals('o', bytes[2]);
    }

    @Test
    void testMathApi() {
        assertEquals(Math.E, eval("Math.E"));
        assertEquals(2.302585092994046, eval("Math.LN10"));
        assertEquals(0.6931471805599453, eval("Math.LN2"));
        assertEquals(1.4426950408889634, eval("Math.LOG2E"));
        assertEquals(Math.PI, eval("Math.PI"));
        assertEquals(0.7071067811865476, eval("Math.SQRT1_2"));
        assertEquals(1.4142135623730951, eval("Math.SQRT2"));
        assertEquals(5, eval("Math.abs(-5)"));
        assertEquals(Math.PI, eval("Math.acos(-1)"));
        assertEquals(Undefined.NAN, eval("Math.acosh(0.5)"));
        assertEquals(1.5667992369724109, (double) eval("Math.acosh(2.5)"), 0.01);
        assertEquals(1.5707963267948966, eval("Math.asin(1)"));
        assertEquals(0.8813735870195429, eval("Math.asinh(1)"));
        assertEquals(0.7853981633974483, eval("Math.atan(1)"));
        assertEquals(1.4056476493802699, eval("Math.atan2(90, 15)"));
        assertEquals(0.5493061443340548, (double) eval("Math.atanh(0.5)"), 0.01);
        assertEquals(4, eval("Math.cbrt(64)"));
        assertEquals(1, eval("Math.ceil(0.95)"));
        assertEquals(22, eval("Math.clz32(1000)"));
        assertEquals(0.5403023058681398, eval("Math.cos(1)"));
        assertEquals(1.543080634815244, eval("Math.cosh(1)"));
        assertEquals(7.38905609893065, eval("Math.exp(2)"));
        assertEquals(1.718281828459045, eval("Math.expm1(1)"));
        assertEquals(5, eval("Math.floor(5.05)"));
        assertEquals(1.3370000123977661, eval("Math.fround(1.337)"));
        assertEquals(13, eval("Math.hypot(5, 12)"));
        assertEquals(-5, eval("Math.imul(0xffffffff, 5)"));
        assertEquals(2.302585092994046, eval("Math.log(10)"));
        assertEquals(5, eval("Math.log10(100000)"));
        assertEquals(0.6931471805599453, eval("Math.log1p(1)"));
        assertEquals(1.584962500721156, (double) eval("Math.log2(3)"), 0.01);
        assertEquals(6, eval("Math.max(3, 6)"));
        assertEquals(3, eval("Math.min(3, 6)"));
        assertEquals(343, eval("Math.pow(7, 3)"));
        assertEquals(343, eval("Math.pow(7, 3)"));
        assertInstanceOf(Number.class, eval("Math.random()"));
        assertEquals(1, eval("Math.round(0.9)"));
        assertEquals(-0.0, eval("Math.sign(-0)"));
        assertEquals(0, eval("Math.sign(0)"));
        assertEquals(1, eval("Math.sign(100)"));
        assertEquals(-1, eval("Math.sign(-20)"));
        assertEquals(0.8414709848078965, eval("Math.sin(1)"));
        assertEquals(1.1752011936438014, eval("Math.sinh(1)"));
        assertEquals(1.4142135623730951, eval("Math.sqrt(2)"));
        assertEquals(1.5574077246549023, eval("Math.tan(1)"));
        assertEquals(0.7615941559557649, eval("Math.tanh(1)"));
        assertEquals(1, eval("Math.trunc(1.9)"));
        assertEquals(-1, eval("Math.trunc(-1.9)"));
        assertEquals(-0.0, eval("Math.trunc(-0.9)"));
    }

    @Test
    void testJsonApi() {
        assertEquals("{\"a\":\"b\"}", eval("JSON.stringify({a:'b'})"));
        assertEquals(Map.of("a", "b"), eval("JSON.parse('{\"a\":\"b\"}')"));
    }

    @Test
    void testParseInt() {
        assertEquals(42, eval("parseInt('042')"));
    }

    @Test
    void testIfStatement() {
        eval("if (true) a = 1");
        assertEquals(1, get("a"));
        eval("if (false) a = 1");
        assertEquals(Undefined.INSTANCE, get("a"));
        eval("if (false) a = 1; else a = 2");
        assertEquals(2, get("a"));
        eval("a = 1; if (a) b = 2");
        assertEquals(2, get("b"));
        eval("a = 0; if (a) b = 2");
        assertEquals(Undefined.INSTANCE, get("b"));
        eval("a = ''; if (a) b = 1; else b = 2");
        assertEquals(2, get("b"));
        assertEquals(true, eval("if (false) { false } else { true }"));
        assertEquals(false, eval("if (false) { false } else { false }"));
    }

    @Test
    void testForLoop() {
        eval("a = 0; for (var i = 0; i < 5; i++) a++");
        assertEquals(5, get("a"));
    }

    @Test
    void testForInLoop() {
        eval("var a = []; for (var x in [1, 2, 3]) a.push(x)");
        match(get("a"), "['0', '1', '2']");
    }

    @Test
    void testForOfLoop() {
        eval("var a = []; var x; for (x of [1, 2, 3]) a.push(x)");
        match(get("a"), "[1, 2, 3]");
    }

    @Test
    void testWhileLoop() {
        eval("a = 0; while(a < 5) a++");
        assertEquals(5, get("a"));
    }

    @Test
    void testDoWhileLoop() {
        eval("a = 0; do { a++ } while (a <= 5)");
        assertEquals(6, get("a"));
    }

    @Test
    void testTernary() {
        eval("a = true ? 1 : 2");
        assertEquals(1, get("a"));
        eval("a = false ? 1 : 2");
        assertEquals(2, get("a"));
        eval("a = 5; b = a > 3 ? 'foo' : 4 + 5");
        assertEquals("foo", get("b"));
        eval("a = 5; b = a < 3 ? 'foo' : 4 + 5");
        assertEquals(9, get("b"));
    }

    @Test
    void testTypeOf() {
        assertEquals("string", eval("typeof 'foo'"));
        assertEquals("function", eval("var a = function(){}; typeof a"));
        assertEquals("object", eval("typeof new Error('foo')"));
        assertEquals(true, eval("typeof 'foo' === 'string'"));
        assertEquals("undefined", eval("typeof bar"));
    }

    @Test
    void testTryCatch() {
        eval("var a; try { throw 'foo' } catch (e) { a = e }");
        assertEquals("foo", get("a"));
        eval("var a, b; try { throw 'foo' } catch (e) { a = e; if (true) return; a = null } finally { b = 2 }");
        assertEquals("foo", get("a"));
        assertEquals(2, get("b"));
        eval("var a; try { } finally { a = 3 }");
        assertEquals(3, get("a"));
        eval("var a; try { throw 'foo' } catch { a = 'bar' }");
        assertEquals("bar", get("a"));
    }

    @Test
    void testDotExpressionUndefined() {
        assertEquals(Undefined.INSTANCE, eval("var foo = {}; foo.bar"));
        assertEquals(Undefined.INSTANCE, eval("foo.bar"));
    }

    @Test
    void testThrow() {
        try {
            eval("function a(b){ b() }; a(() => { throw new Error('foo') })");
            fail("error expected");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("foo"));
        }
    }

    @Test
    void testThrowFunction() {
        try {
            eval("function a(b){ this.bar = 'baz'; b() }; a(function(){ throw new Error('foo') })");
            fail("error expected");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("foo"));
            JsFunction fn = (JsFunction) get("a");
            assertEquals("baz", fn.get("bar"));
        }
    }

    @Test
    void testSwitch() {
        eval("var a = 2; var b; switch (a) { case 2: b = 1 }");
        assertEquals(1, get("b"));
        eval("var a = 2; var b; switch (a) { case 1: b = 1; break; case 2: b = 2; break }");
        assertEquals(2, get("b"));
        eval("var a = 2; var b; switch (a) { case 1: b = 1; break; default: b = 2 }");
        assertEquals(2, get("b"));
        eval("var a = 1; var b; switch (a) { case 1: b = 1; default: b = 2 }");
        assertEquals(2, get("b"));
    }

    @Test
    void testArrayApi() {
        match(eval("[1, 2, 3].length"), "3");
        match(eval("[1, 2, 3].map(x => x * 2)"), "[2, 4, 6]");
        match(eval("[].map.call([1, 2, 3], x => x * 2)"), "[2, 4, 6]");
        match(eval("[].map.call([1, 2, 3], String)"), "['1', '2', '3']");
        match(eval("[1, 2, 3].join()"), "1,2,3");
        match(eval("[1, 2, 3].join(', ')"), "1, 2, 3");
        match(eval("[].map.call({0:'a',1:'b'}, (x, i) => x + i)"), "['a0','b1']");
        match(eval("Array.from([1, 2, 3])"), "[1, 2, 3]");
        match(eval("Array.from([1, 2, 3], x => x * 2)"), "[2, 4, 6]");
        match(eval("Array.from({ length: 3 }, (v, i) => i)"), "[0, 1, 2]");
        assertEquals(2, eval("[1, 2, 3].find(x => x % 2 === 0)"));
        assertEquals(Undefined.INSTANCE, eval("[1, 2, 3].find(x => x % 5 === 0)"));
        assertEquals(4, eval("[1, 2, 3].push(2)"));
        eval("var a = []; var b = a.push(1, 2, 3);");
        match(get("a"), "[1, 2, 3]");
        assertEquals(3, get("b"));
        match(eval("[1, 2, 3].reverse()"), "[3, 2, 1]");
        assertEquals(true, eval("[1, 2, 3].includes(2)"));
        assertEquals(1, eval("[1, 2, 3].indexOf(2)"));
        assertEquals(-1, eval("[1, 2, 3].indexOf(5)"));
    }

    @Test
    void testObjectApi() {
        match(eval("Object.keys({ a: 1, b: 2 })"), "['a', 'b']");
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
