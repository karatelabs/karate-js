package io.karatelabs.js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class JsMathTest extends EvalBase {

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

}
