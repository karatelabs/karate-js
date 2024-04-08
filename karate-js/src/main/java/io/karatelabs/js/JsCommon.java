/*
 * The MIT License
 *
 * Copyright 2024 Karate Labs Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.karatelabs.js;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

public class JsCommon {

    static final Invokable ARRAY_CONSTRUCTOR = (instance, args) -> new JsArray();

    static boolean instanceOf(Object lhs, Object rhs) {
        if (lhs instanceof JsObject && rhs instanceof JsObject) {
            JsObject objectLhs = (JsObject) lhs;
            Map<String, Object> prototypeLhs = objectLhs.getPrototype();
            if (prototypeLhs != null) {
                Object constructorLhs = prototypeLhs.get("constructor");
                if (constructorLhs != null) {
                    JsObject objectRhs = (JsObject) rhs;
                    Object constructorRhs = objectRhs.get("constructor");
                    return constructorLhs == constructorRhs;
                }
            }
        }
        return false;
    }

    static final Invokable STRING_CONSTRUCTOR = (instance, args) -> {
        if (args.length > 0 && args[0] != null) {
            return args[0].toString();
        }
        return null;
    };

    static final JsFunction TO_STRING = new JsFunction() {
        @Override
        public Object invoke(Object instance, Object... args) {
            if (instance == null) {
                return "[object Null]";
            }
            if (instance instanceof ArrayLike || instance instanceof List) {
                return "[object Array]";
            }
            if (instance instanceof String || instance instanceof Number || instance instanceof Boolean) {
                return instance.toString();
            }
            return "[object Object]";
        }
    };

    static final JsObject CONSOLE = createConsole();

    private static JsObject createConsole() {
        JsObject object = new JsObject();
        object.put("log", new Invokable() {
            @Override
            public Object invoke(Object instance, Object... args) {
                StringBuilder sb = new StringBuilder();
                for (Object arg : args) {
                    if (arg instanceof ObjectLike) {
                        Object toString = ((ObjectLike) arg).get("toString");
                        if (toString instanceof Invokable) {
                            sb.append(((Invokable) toString).invoke(arg));
                        } else {
                            sb.append(TO_STRING.invoke(arg));
                        }
                    } else {
                        sb.append(TO_STRING.invoke(arg));
                    }
                    sb.append(' ');
                }
                System.out.println(sb);
                return null;
            }
        });
        return object;
    }

    static final JsObject GLOBAL_OBJECT = new JsObject();

    static final JsArray GLOBAL_ARRAY = new JsArray() {
        @Override
        Map<String, Object> initPrototype() {
            Map<String, Object> prototype = super.initPrototype();
            prototype.put("from", new JsFunction() {
                @Override
                public Object invoke(Object instance, Object... args) {
                    List<Object> results = new ArrayList<>();
                    Invokable invokable;
                    if (args.length > 1 && args[1] instanceof Invokable) {
                        invokable = (Invokable) args[1];
                    } else {
                        invokable = null;
                    }
                    loop(null, args[0], invokable, r -> results.add(r.result));
                    return results;
                }
            });
            return prototype;
        }
    };

    static class LoopResult {

        final Object element;
        final int index;
        final Object result;

        LoopResult(Object element, int index, Object result) {
            this.element = element;
            this.index = index;
            this.result = result;
        }
    }

    static Invokable toInvokable(Object[] args) {
        if (args.length > 0) {
            if (args[0] instanceof Invokable) {
                return (Invokable) args[0];
            }
        }
        return null;
    }

    static ArrayLike toArrayLike(Map<String, Object> map) {
        List<Object> list = new ArrayList<>();
        if (map.containsKey("length")) {
            Object length = map.get("length");
            if (length instanceof Number) {
                int size = ((Number) length).intValue();
                for (int i = 0; i < size; i++) {
                    list.add(0, Undefined.INSTANCE);
                }
            }
        }
        Set<Integer> indexes = new HashSet<>();
        for (String key : map.keySet()) {
            try {
                int index = Integer.parseInt(key);
                indexes.add(index);
            } catch (Exception e) {

            }
        }
        for (int index : indexes) {
            list.add(index, map.get(index + ""));
        }
        return new JsArray(list);
    }

    @SuppressWarnings("unchecked")
    private static ArrayLike thisArray(ArrayLike array, Object instance) {
        if (instance instanceof List) {
            return new JsArray((List<Object>) instance);
        } else if (instance instanceof ArrayLike) {
            return (ArrayLike) instance;
        } else if (instance instanceof Map) {
            return toArrayLike((Map<String, Object>) instance);
        }
        return array;
    }

    static void loop(ArrayLike array, Object instance, Invokable invokable, Function<LoopResult, Boolean> action) {
        array = thisArray(array, instance);
        int count = array.size();
        for (int i = 0; i < count; i++) {
            Object value = array.get(i);
            Object result = invokable == null ? value : invokable.invoke(null, value, i);
            Boolean doNext = action.apply(new LoopResult(value, i, result));
            if (doNext != null && !doNext) { // return a boolean false to stop loop
                break;
            }
        }
    }

    static final Object[] EMPTY = new Object[0];

    static class ShiftArgs {

        final Object instance;
        final Object[] args;

        ShiftArgs(Object[] args) {
            if (args.length == 0) {
                instance = null;
                this.args = EMPTY;
            } else {
                List<Object> list = new ArrayList<>(Arrays.asList(args));
                instance = list.remove(0);
                this.args = list.toArray();
            }
        }

    }

    @SuppressWarnings("unchecked")
    static Iterable<KeyValue> toIterable(Object object) {
        if (object instanceof Map) {
            return new Iterable<>() {
                @Override
                public Iterator<KeyValue> iterator() {
                    final Iterator<Map.Entry<String, Object>> entries = ((Map<String, Object>) object).entrySet().iterator();
                    return new Iterator<>() {
                        int index = 0;

                        @Override
                        public boolean hasNext() {
                            return entries.hasNext();
                        }

                        @Override
                        public KeyValue next() {
                            Map.Entry<String, Object> entry = entries.next();
                            return new KeyValue(object, index++, entry.getKey(), entry.getValue());
                        }
                    };
                }
            };
        } else if (object instanceof ObjectLike) {
            return () -> {
                final ObjectLike objectLike = ((ObjectLike) object);
                final Iterator<String> keys = objectLike.keys().iterator();
                return new Iterator<>() {
                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return keys.hasNext();
                    }

                    @Override
                    public KeyValue next() {
                        String key = keys.next();
                        return new KeyValue(object, index++, key, objectLike.get(key));
                    }
                };
            };
        } else if (object instanceof List) {
            return () -> {
                final Iterator<Object> items = ((List<Object>) object).iterator();
                return new Iterator<>() {
                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return items.hasNext();
                    }

                    @Override
                    public KeyValue next() {
                        int i = index++;
                        return new KeyValue(object, i, i + "", items.next());
                    }
                };
            };
        } else if (object instanceof ArrayLike) {
            return () -> {
                final ArrayLike arrayLike = (ArrayLike) object;
                final int size = arrayLike.size();
                return new Iterator<>() {
                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < size;
                    }

                    @Override
                    public KeyValue next() {
                        int i = index++;
                        return new KeyValue(object, i, i + "", arrayLike.get(i));
                    }
                };
            };
        } else {
            return null;
        }
    }

    static Map<String, Object> getBaseArrayPrototype(ArrayLike array) {
        Map<String, Object> prototype = new HashMap<>();
        prototype.put("toString", TO_STRING);
        prototype.put("constructor", ARRAY_CONSTRUCTOR);
        prototype.put("length", new Property(array::size));
        prototype.put("map", new JsFunction() {
            @Override
            public Object invoke(Object instance, Object... args) {
                List<Object> results = new ArrayList<>();
                loop(array, instance, toInvokable(args), r -> results.add(r.result));
                return results;
            }
        });
        prototype.put("filter", new JsFunction() {
            @Override
            public Object invoke(Object instance, Object... args) {
                List<Object> results = new ArrayList<>();
                loop(array, instance, toInvokable(args), r -> {
                    if (Terms.isTruthy(r.result)) {
                        results.add(r.element);
                    }
                    return null;
                });
                return results;
            }
        });
        prototype.put("join", new JsFunction() {
            @Override
            public Object invoke(Object instance, Object... args) {
                StringBuilder sb = new StringBuilder();
                String delimiter;
                if (args.length > 0 && args[0] != null) {
                    delimiter = args[0].toString();
                } else {
                    delimiter = ",";
                }
                loop(array, instance, null, r -> {
                    if (sb.length() != 0) {
                        sb.append(delimiter);
                    }
                    sb.append(r.element);
                    return null;
                });
                return sb.toString();
            }
        });
        prototype.put("find", new JsFunction() {
            @Override
            public Object invoke(Object instance, Object... args) {
                AtomicReference<Object> result = new AtomicReference<>(Undefined.INSTANCE);
                loop(array, instance, toInvokable(args), r -> {
                    if (Terms.isTruthy(r.result)) {
                        result.set(r.element);
                        return false;
                    }
                    return null;
                });
                return result.get();
            }
        });
        prototype.put("push", new JsFunction() {
            @Override
            public Object invoke(Object instance, Object... args) {
                ArrayLike thisArray = thisArray(array, instance);
                for (Object o : args) {
                    thisArray.add(o);
                }
                return thisArray.size();
            }
        });
        return prototype;
    }

    static Map<String, Object> getBaseObjectPrototype(JsObject object) {
        Map<String, Object> prototype = new HashMap<>();
        prototype.put("toString", TO_STRING);
        prototype.put("constructor", new JsFunction() {
            @Override
            public Object invoke(Object instance, Object... args) {
                return new JsObject();
            }

            @Override
            public boolean equals(Object obj) {
                return object.equals(obj);
            }
        });
        return prototype;
    }

    static Map<String, Object> getBaseFunctionPrototype(JsFunction function) {
        Map<String, Object> prototype = new HashMap<>();
        prototype.put("toString", TO_STRING);
        prototype.put("constructor", new JsFunction() {
            @Override
            public Object invoke(Object instance, Object... args) {
                return function.invoke(instance, args);
            }

            @Override
            public boolean equals(Object obj) {
                return function.equals(obj);
            }
        });
        prototype.put("call", (Invokable) (instance, args) -> {
            ShiftArgs shifted = new ShiftArgs(args);
            return function.invoke(shifted.instance, shifted.args);
        });
        return prototype;
    }

    private static Invokable math(Function<Double, Double> fn) {
        return (instance, args) -> {
            try {
                Number x = Terms.toNumber(args[0]);
                Double y = fn.apply(x.doubleValue());
                return Terms.narrow(y);
            } catch (Exception e) {
                return Terms.NAN;
            }
        };
    }

    private static Invokable math(BiFunction<Double, Double, Double> fn) {
        return (instance, args) -> {
            try {
                Number x = Terms.toNumber(args[0]);
                Number y = Terms.toNumber(args[1]);
                Double r = fn.apply(x.doubleValue(), y.doubleValue());
                return Terms.narrow(r);
            } catch (Exception e) {
                return Terms.NAN;
            }
        };
    }

    static final JsObject MATH = new JsObject() {
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
            prototype.put("clz32", (Invokable) (instance, args) -> {
                Number x = Terms.toNumber(args[0]);
                return Integer.numberOfLeadingZeros(x.intValue());
            });
            prototype.put("cos", math(Math::cos));
            prototype.put("cosh", math(Math::cosh));
            prototype.put("exp", math(Math::exp));
            prototype.put("expm1", math(Math::expm1));
            prototype.put("floor", math(Math::floor));
            prototype.put("fround", (Invokable) (instance, args) -> {
                Number x = Terms.toNumber(args[0]);
                float y = (float) x.doubleValue();
                return (double) y;
            });
            prototype.put("hypot", math(Math::hypot));
            prototype.put("imul", (Invokable) (instance, args) -> {
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
            prototype.put("random", (Invokable) (instance, args) -> Math.random());
            prototype.put("round", (Invokable) (instance, args) -> {
                Number x = Terms.toNumber(args[0]);
                return Terms.narrow(Math.round(x.doubleValue()));
            });
            prototype.put("sign", (Invokable) (instance, args) -> {
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
    };

}
