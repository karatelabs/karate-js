package io.karatelabs.js;

import java.util.function.BiFunction;
import java.util.function.Function;

public class JsMath extends JsObject {

    @Override
    Prototype initPrototype() {
        Prototype wrapped = super.initPrototype();
        return new Prototype(wrapped) {
            @Override
            public Object getProperty(String propName) {
                switch (propName) {
                    case "E":
                        return Math.E;
                    case "LN10":
                        return Math.log(10);
                    case "LN2":
                        return Math.log(2);
                    case "LOG2E":
                        return 1 / Math.log(2);
                    case "PI":
                        return Math.PI;
                    case "SQRT1_2":
                        return Math.sqrt(0.5);
                    case "SQRT2":
                        return Math.sqrt(2);
                    case "abs":
                        return math(Math::abs);
                    case "acos":
                        return math(Math::acos);
                    case "acosh":
                        return math(x -> {
                            if (x < 1) {
                                throw new RuntimeException("value must be >= 1");
                            }
                            return Math.log(x + Math.sqrt(x * x - 1));
                        });
                    case "asin":
                        return math(Math::asin);
                    case "asinh":
                        return math(x -> Math.log(x + Math.sqrt(x * x + 1)));
                    case "atan":
                        return math(Math::atan);
                    case "atan2":
                        return math(Math::atan2);
                    case "atanh":
                        return math(x -> {
                            if (x <= -1 || x >= 1) {
                                throw new RuntimeException("value must be between -1 and 1 (exclusive)");
                            }
                            return 0.5 * Math.log((1 + x) / (1 - x));
                        });
                    case "cbrt":
                        return math(Math::cbrt);
                    case "ceil":
                        return math(Math::ceil);
                    case "clz32":
                        return (Invokable) args -> {
                            Number x = Terms.toNumber(args[0]);
                            return Integer.numberOfLeadingZeros(x.intValue());
                        };
                    case "cos":
                        return math(Math::cos);
                    case "cosh":
                        return math(Math::cosh);
                    case "exp":
                        return math(Math::exp);
                    case "expm1":
                        return math(Math::expm1);
                    case "floor":
                        return math(Math::floor);
                    case "fround":
                        return (Invokable) args -> {
                            Number x = Terms.toNumber(args[0]);
                            float y = (float) x.doubleValue();
                            return (double) y;
                        };
                    case "hypot":
                        return math(Math::hypot);
                    case "imul":
                        return (Invokable) args -> {
                            Number x = Terms.toNumber(args[0]);
                            Number y = Terms.toNumber(args[1]);
                            return x.intValue() * y.intValue();
                        };
                    case "log":
                        return math(Math::log);
                    case "log10":
                        return math(Math::log10);
                    case "log1p":
                        return math(Math::log1p);
                    case "log2":
                        return math(x -> Math.log(x) / Math.log(2));
                    case "max":
                        return math(Math::max);
                    case "min":
                        return math(Math::min);
                    case "pow":
                        return math(Math::pow);
                    case "random":
                        return (Invokable) args -> Math.random();
                    case "round":
                        return (Invokable) args -> {
                            Number x = Terms.toNumber(args[0]);
                            return Terms.narrow(Math.round(x.doubleValue()));
                        };
                    case "sign":
                        return (Invokable) args -> {
                            Number x = Terms.toNumber(args[0]);
                            if (Terms.NEGATIVE_ZERO.equals(x)) {
                                return Terms.NEGATIVE_ZERO;
                            }
                            if (Terms.POSITIVE_ZERO.equals(x)) {
                                return Terms.POSITIVE_ZERO;
                            }
                            return x.doubleValue() > 0 ? 1 : -1;
                        };
                    case "sin":
                        return math(Math::sin);
                    case "sinh":
                        return math(Math::sinh);
                    case "sqrt":
                        return math(Math::sqrt);
                    case "tan":
                        return math(Math::tan);
                    case "tanh":
                        return math(Math::tanh);
                    case "trunc":
                        return math(x -> x > 0 ? Math.floor(x) : Math.ceil(x));
                }
                return null;
            }
        };
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
