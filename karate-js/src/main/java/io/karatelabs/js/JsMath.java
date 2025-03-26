package io.karatelabs.js;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class JsMath extends JsObject {

    @Override
    Map<String, Object> initPrototype() {
        Map<String, Object> prototype = super.initPrototype();
        prototype.put("E", Math.E);
        prototype.put("LN10", Math.log(10));
        prototype.put("LN2", Math.log(2));
        prototype.put("LOG2E", 1 / Math.log(2));
        prototype.put("PI", Math.PI);
        prototype.put("SQRT1_2", Math.sqrt(0.5));
        prototype.put("SQRT2", Math.sqrt(2));
        prototype.put("abs", math(Math::abs));
        prototype.put("acos", math(Math::acos));
        prototype.put("acosh", math(x -> {
            if (x < 1) {
                throw new RuntimeException("value must be >= 1");
            }
            return Math.log(x + Math.sqrt(x * x - 1));
        }));
        prototype.put("asin", math(Math::asin));
        prototype.put("asinh", math(x -> Math.log(x + Math.sqrt(x * x + 1))));
        prototype.put("atan", math(Math::atan));
        prototype.put("atan2", math(Math::atan2));
        prototype.put("atanh", math(x -> {
            if (x <= -1 || x >= 1) {
                throw new RuntimeException("value must be between -1 and 1 (exclusive)");
            }
            return 0.5 * Math.log((1 + x) / (1 - x));
        }));
        prototype.put("cbrt", math(Math::cbrt));
        prototype.put("ceil", math(Math::ceil));
        prototype.put("clz32", (Invokable) args -> {
            Number x = Terms.toNumber(args[0]);
            return Integer.numberOfLeadingZeros(x.intValue());
        });
        prototype.put("cos", math(Math::cos));
        prototype.put("cosh", math(Math::cosh));
        prototype.put("exp", math(Math::exp));
        prototype.put("expm1", math(Math::expm1));
        prototype.put("floor", math(Math::floor));
        prototype.put("fround", (Invokable) args -> {
            Number x = Terms.toNumber(args[0]);
            float y = (float) x.doubleValue();
            return (double) y;
        });
        prototype.put("hypot", math(Math::hypot));
        prototype.put("imul", (Invokable) args -> {
            Number x = Terms.toNumber(args[0]);
            Number y = Terms.toNumber(args[1]);
            return x.intValue() * y.intValue();
        });
        prototype.put("log", math(Math::log));
        prototype.put("log10", math(Math::log10));
        prototype.put("log1p", math(Math::log1p));
        prototype.put("log2", math(x -> Math.log(x) / Math.log(2)));
        prototype.put("max", math(Math::max));
        prototype.put("min", math(Math::min));
        prototype.put("pow", math(Math::pow));
        prototype.put("random", (Invokable) args -> Math.random());
        prototype.put("round", (Invokable) args -> {
            Number x = Terms.toNumber(args[0]);
            return Terms.narrow(Math.round(x.doubleValue()));
        });
        prototype.put("sign", (Invokable) args -> {
            Number x = Terms.toNumber(args[0]);
            if (Terms.NEGATIVE_ZERO.equals(x)) {
                return Terms.NEGATIVE_ZERO;
            }
            if (Terms.POSITIVE_ZERO.equals(x)) {
                return Terms.POSITIVE_ZERO;
            }
            return x.doubleValue() > 0 ? 1 : -1;
        });
        prototype.put("sin", math(Math::sin));
        prototype.put("sinh", math(Math::sinh));
        prototype.put("sqrt", math(Math::sqrt));
        prototype.put("tan", math(Math::tan));
        prototype.put("tanh", math(Math::tanh));
        prototype.put("trunc", math(x -> x > 0 ? Math.floor(x) : Math.ceil(x)));
        return prototype;
    }

    private static Invokable math(Function<Double, Double> fn) {
        return args -> {
            try {
                Number x = Terms.toNumber(args[0]);
                Double y = fn.apply(x.doubleValue());
                return Terms.narrow(y);
            } catch (Exception e) {
                return Undefined.NAN;
            }
        };
    }

    private static Invokable math(BiFunction<Double, Double, Double> fn) {
        return args -> {
            try {
                Number x = Terms.toNumber(args[0]);
                Number y = Terms.toNumber(args[1]);
                Double r = fn.apply(x.doubleValue(), y.doubleValue());
                return Terms.narrow(r);
            } catch (Exception e) {
                return Undefined.NAN;
            }
        };
    }

}
