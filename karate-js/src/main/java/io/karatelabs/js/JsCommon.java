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

import net.minidev.json.JSONValue;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class JsCommon {

    static final Invokable ARRAY_CONSTRUCTOR = args -> new JsArray();

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

    static final JsObject GLOBAL_STRING = new JsString("");

    static String TO_STRING(Object o) {
        if (o == null) {
            return "[object Null]";
        }
        if (o instanceof List) {
            return "[object Array]";
        }
        if (Terms.isPrimitive(o)) {
            return o.toString();
        }
        return "[object Object]";
    }

    static final JsObject JAVA_GLOBAL = createJavaGlobal();

    private static JsObject createJavaGlobal() {
        JsObject object = new JsObject();
        object.put("type", (Invokable) args -> new JavaClass((String) args[0]));
        return object;
    }

    static JsObject createConsole(Consumer<String> logger) {
        JsObject object = new JsObject();
        object.put("log", (Invokable) args -> {
            StringBuilder sb = new StringBuilder();
            for (Object arg : args) {
                if (arg instanceof ObjectLike) {
                    Object toString = ((ObjectLike) arg).get("toString");
                    if (toString instanceof Invokable) {
                        sb.append(((Invokable) toString).invoke(arg));
                    } else {
                        sb.append(TO_STRING(arg));
                    }
                } else {
                    sb.append(TO_STRING(arg));
                }
                sb.append(' ');
            }
            logger.accept(sb.toString());
            return null;
        });
        return object;
    }

    static final JsObject JSON = createJson();

    private static JsObject createJson() {
        JsObject object = new JsObject();
        object.put("stringify", (Invokable) args -> JSONValue.toJSONString(args[0]));
        object.put("parse", (Invokable) args -> JSONValue.parse((String) args[0]));
        return object;
    }

    static final JsObject GLOBAL_OBJECT = new JsObject() {
        @Override
        Map<String, Object> initPrototype() {
            Map<String, Object> prototype = super.initPrototype();
            prototype.put("keys", new JsFunction() {
                @Override
                public Object invoke(Object... args) {
                    List<Object> result = new ArrayList<>();
                    for (KeyValue kv : toIterable(args[0])) {
                        result.add(kv.key);
                    }
                    return result;
                }
            });
            return prototype;
        }
    };

    @SuppressWarnings("unchecked")
    static final JsArray GLOBAL_ARRAY = new JsArray() {
        @Override
        Map<String, Object> initPrototype() {
            Map<String, Object> prototype = super.initPrototype();
            prototype.put("from", new JsFunction() {
                @Override
                public Object invoke(Object... args) {
                    List<Object> results = new ArrayList<>();
                    Invokable invokable;
                    if (args.length > 1 && args[1] instanceof Invokable) {
                        invokable = (Invokable) args[1];
                    } else {
                        invokable = null;
                    }
                    Object array = args[0];
                    if (array instanceof Map) {
                        array = toArrayLike((Map<String, Object>) args[0]);
                    }
                    for (KeyValue kv : toIterable(array)) {
                        Object result = invokable == null ? kv.value : invokable.invoke(kv.value, kv.index);
                        results.add(result);
                    }
                    return results;
                }
            });
            prototype.put("isArray", new JsFunction() {
                @Override
                public Object invoke(Object... args) {
                    if (args.length == 0) {
                        return false;
                    }
                    Object arg = args[0];
                    return arg instanceof List || arg instanceof ArrayLike;
                }
            });
            prototype.put("of", new JsFunction() {
                @Override
                public Object invoke(Object... args) {
                    return Arrays.asList(args);
                }
            });
            return prototype;
        }
    };

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

    static final Object[] EMPTY = new Object[0];

    static class ShiftArgs {

        final Object thisObject;
        final Object[] args;

        ShiftArgs(Object[] args) {
            if (args.length == 0) {
                thisObject = null;
                this.args = EMPTY;
            } else {
                List<Object> list = new ArrayList<>(Arrays.asList(args));
                thisObject = list.remove(0);
                this.args = list.toArray();
            }
        }

    }

    @SuppressWarnings("unchecked")
    static Iterable<KeyValue> toIterable(Object object) {
        if (object instanceof Map) {
            return () -> {
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

    static Invokable toInvokable(Object object) {
        if (object instanceof Invokable) {
            return (Invokable) object;
        }
        if (object instanceof Prototype) {
            Prototype prototype = (Prototype) object;
            return (Invokable) prototype.getPrototype().get("constructor");
        }
        return null;
    }

    static Map<String, Object> getBaseArrayPrototype(ArrayLike array) {
        Map<String, Object> prototype = new HashMap<>();
        prototype.put("toString", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                return TO_STRING(thisObject == null ? array : thisObject);
            }
        });
        prototype.put("constructor", ARRAY_CONSTRUCTOR);
        prototype.put("length", new Property(array::size));
        prototype.put("map", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                List<Object> results = new ArrayList<>();
                ArrayLike thisArray = thisArray(array, thisObject);
                Invokable invokable = toInvokable(args[0]);
                for (KeyValue kv : toIterable(thisArray)) {
                    Object result = invokable.invoke(kv.value, kv.index);
                    results.add(result);
                }
                return results;
            }
        });
        prototype.put("filter", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                List<Object> results = new ArrayList<>();
                ArrayLike thisArray = thisArray(array, thisObject);
                Invokable invokable = toInvokable(args[0]);
                for (KeyValue kv : toIterable(thisArray)) {
                    Object result = invokable.invoke(kv.value, kv.index);
                    if (Terms.isTruthy(result)) {
                        results.add(kv.value);
                    }
                }
                return results;
            }
        });
        prototype.put("join", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                StringBuilder sb = new StringBuilder();
                String delimiter;
                if (args.length > 0 && args[0] != null) {
                    delimiter = args[0].toString();
                } else {
                    delimiter = ",";
                }
                ArrayLike thisArray = thisArray(array, thisObject);
                for (KeyValue kv : toIterable(thisArray)) {
                    if (sb.length() != 0) {
                        sb.append(delimiter);
                    }
                    sb.append(kv.value);
                }
                return sb.toString();
            }
        });
        prototype.put("find", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                Invokable invokable = toInvokable(args[0]);
                for (KeyValue kv : toIterable(thisArray)) {
                    Object result = invokable.invoke(kv.value, kv.index);
                    if (Terms.isTruthy(result)) {
                        return kv.value;
                    }
                }
                return Undefined.INSTANCE;
            }
        });
        prototype.put("findIndex", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                Invokable invokable = toInvokable(args[0]);
                for (KeyValue kv : toIterable(thisArray)) {
                    Object result = invokable.invoke(kv.value, kv.index);
                    if (Terms.isTruthy(result)) {
                        return kv.index;
                    }
                }
                return -1;
            }
        });
        prototype.put("push", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                for (Object o : args) {
                    thisArray.add(o);
                }
                return thisArray.size();
            }
        });
        prototype.put("reverse", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                List<Object> result = new ArrayList<>();
                for (int i = size; i > 0; i--) {
                    result.add(thisArray.get(i - 1));
                }
                return result;
            }
        });
        prototype.put("includes", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                for (KeyValue kv : toIterable(thisArray)) {
                    if (Terms.eq(kv.value, args[0], false)) {
                        return true;
                    }
                }
                return false;
            }
        });
        prototype.put("indexOf", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                if (size == 0) {
                    return -1;
                }
                if (args.length == 0) {
                    return -1;
                }
                Object searchElement = args[0];
                int fromIndex = 0;
                if (args.length > 1 && args[1] != null) {
                    fromIndex = Terms.toNumber(args[1]).intValue();
                    if (fromIndex < 0) {
                        fromIndex = Math.max(size + fromIndex, 0);
                    }
                }
                if (fromIndex >= size) {
                    return -1;
                }
                for (int i = fromIndex; i < size; i++) {
                    if (Terms.eq(thisArray.get(i), searchElement, false)) {
                        return i;
                    }
                }
                return -1;
            }
        });
        prototype.put("slice", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                int start = 0;
                int end = size;
                if (args.length > 0 && args[0] != null) {
                    start = Terms.toNumber(args[0]).intValue();
                    if (start < 0) {
                        start = Math.max(size + start, 0);
                    }
                }
                if (args.length > 1 && args[1] != null) {
                    end = Terms.toNumber(args[1]).intValue();
                    if (end < 0) {
                        end = Math.max(size + end, 0);
                    }
                }
                start = Math.min(start, size);
                end = Math.min(end, size);
                List<Object> result = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    result.add(thisArray.get(i));
                }
                return result;
            }
        });
        prototype.put("forEach", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                Invokable invokable = toInvokable(args[0]);
                for (KeyValue kv : toIterable(thisArray)) {
                    invokable.invoke(kv.value, kv.index, thisArray);
                }
                return Undefined.INSTANCE;
            }
        });
        prototype.put("concat", new JsFunction() {
            @Override
            @SuppressWarnings("unchecked")
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                List<Object> result = new ArrayList<>(thisArray.toList());
                for (Object arg : args) {
                    if (arg instanceof List) {
                        result.addAll((List<Object>) arg);
                    } else if (arg instanceof ArrayLike) {
                        result.addAll(((ArrayLike) arg).toList());
                    } else {
                        result.add(arg);
                    }
                }
                return result;
            }
        });
        prototype.put("every", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                if (thisArray.size() == 0) {
                    return true;
                }
                Invokable invokable = toInvokable(args[0]);
                for (KeyValue kv : toIterable(thisArray)) {
                    Object result = invokable.invoke(kv.value, kv.index, thisArray);
                    if (!Terms.isTruthy(result)) {
                        return false;
                    }
                }
                return true;
            }
        });
        prototype.put("some", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                if (thisArray.size() == 0) {
                    return false;
                }
                Invokable invokable = toInvokable(args[0]);
                for (KeyValue kv : toIterable(thisArray)) {
                    Object result = invokable.invoke(kv.value, kv.index, thisArray);
                    if (Terms.isTruthy(result)) {
                        return true;
                    }
                }
                return false;
            }
        });
        prototype.put("reduce", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                Invokable callback = toInvokable(args[0]);
                if (thisArray.size() == 0 && args.length < 2) {
                    throw new RuntimeException("reduce() called on empty array with no initial value");
                }
                int startIndex = 0;
                Object accumulator;
                if (args.length >= 2) {
                    accumulator = args[1];
                } else {
                    accumulator = thisArray.get(0);
                    startIndex = 1;
                }
                for (int i = startIndex; i < thisArray.size(); i++) {
                    Object currentValue = thisArray.get(i);
                    accumulator = callback.invoke(accumulator, currentValue, i, thisArray);
                }
                return accumulator;
            }
        });
        prototype.put("reduceRight", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                Invokable callback = toInvokable(args[0]);
                if (thisArray.size() == 0 && args.length < 2) {
                    throw new RuntimeException("reduceRight() called on empty array with no initial value");
                }
                int startIndex = thisArray.size() - 1;
                Object accumulator;
                if (args.length >= 2) {
                    accumulator = args[1];
                } else {
                    accumulator = thisArray.get(startIndex);
                    startIndex--;
                }
                for (int i = startIndex; i >= 0; i--) {
                    Object currentValue = thisArray.get(i);
                    accumulator = callback.invoke(accumulator, currentValue, i, thisArray);
                }
                return accumulator;
            }
        });
        prototype.put("flat", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                int depth = 1;
                if (args.length > 0 && args[0] != null) {
                    Number depthNum = Terms.toNumber(args[0]);
                    if (!Double.isNaN(depthNum.doubleValue()) && !Double.isInfinite(depthNum.doubleValue())) {
                        depth = depthNum.intValue();
                    }
                }
                List<Object> result = new ArrayList<>();
                flattenArray(thisArray.toList(), result, depth);
                return result;
            }
        });
        prototype.put("flatMap", new JsFunction() {
            @SuppressWarnings("unchecked")
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                Invokable callback = toInvokable(args[0]);
                if (callback == null) {
                    throw new RuntimeException("flatMap() requires a callback function");
                }
                List<Object> mappedResult = new ArrayList<>();
                int index = 0;
                for (Object item : thisArray.toList()) {
                    Object mapped = callback.invoke(item, index, thisArray);
                    if (mapped instanceof List || mapped instanceof ArrayLike) {
                        List<Object> nestedList;
                        if (mapped instanceof ArrayLike) {
                            nestedList = ((ArrayLike) mapped).toList();
                        } else {
                            nestedList = (List<Object>) mapped;
                        }
                        mappedResult.addAll(nestedList);
                    } else {
                        mappedResult.add(mapped);
                    }
                    index++;
                }
                return mappedResult;
            }
        });
        prototype.put("sort", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                List<Object> list = new ArrayList<>(thisArray.toList());
                if (list.isEmpty()) {
                    return list;
                }
                if (args.length > 0 && args[0] instanceof Invokable) {
                    Invokable compareFn = (Invokable) args[0];
                    list.sort((a, b) -> {
                        Object result = compareFn.invoke(a, b);
                        if (result instanceof Number) {
                            return ((Number) result).intValue();
                        }
                        return 0;
                    });
                } else {
                    list.sort((a, b) -> {
                        String strA = a != null ? a.toString() : "undefined";
                        String strB = b != null ? b.toString() : "undefined";
                        return strA.compareTo(strB);
                    });
                }
                // in js, sort modifies the original array and returns it
                // since we're creating a new list, we need to update the original array
                for (int i = 0; i < list.size(); i++) {
                    if (i < thisArray.size()) {
                        thisArray.set(i, list.get(i));
                    } else {
                        thisArray.add(list.get(i));
                    }
                }
                return thisArray.toList();
            }
        });
        prototype.put("fill", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                if (args.length == 0) {
                    return thisObject;
                }
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                if (size == 0) {
                    return thisArray.toList();
                }
                Object value = args[0];
                int start = 0;
                int end = size;
                if (args.length > 1 && args[1] != null) {
                    start = Terms.toNumber(args[1]).intValue();
                    if (start < 0) {
                        start = Math.max(size + start, 0);
                    }
                }
                if (args.length > 2 && args[2] != null) {
                    end = Terms.toNumber(args[2]).intValue();
                    if (end < 0) {
                        end = Math.max(size + end, 0);
                    }
                }
                start = Math.min(start, size);
                end = Math.min(end, size);
                for (int i = start; i < end; i++) {
                    thisArray.set(i, value);
                }
                return thisArray.toList();
            }
        });
        prototype.put("splice", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                if (args.length == 0) {
                    return new ArrayList<>();
                }
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                if (size == 0) {
                    return new ArrayList<>();
                }
                int start = 0;
                if (args[0] != null) {
                    start = Terms.toNumber(args[0]).intValue();
                    if (start < 0) {
                        start = Math.max(size + start, 0);
                    }
                }
                start = Math.min(start, size);
                int deleteCount = size - start;
                if (args.length > 1 && args[1] != null) {
                    deleteCount = Terms.toNumber(args[1]).intValue();
                    deleteCount = Math.min(Math.max(deleteCount, 0), size - start);
                }
                List<Object> elementsToAdd = new ArrayList<>();
                if (args.length > 2) {
                    for (int i = 2; i < args.length; i++) {
                        elementsToAdd.add(args[i]);
                    }
                }
                List<Object> removedElements = new ArrayList<>();
                for (int i = start; i < start + deleteCount; i++) {
                    removedElements.add(thisArray.get(i));
                }
                int newSize = size - deleteCount + elementsToAdd.size();
                List<Object> newList = new ArrayList<>(newSize);
                for (int i = 0; i < start; i++) {
                    newList.add(thisArray.get(i));
                }
                newList.addAll(elementsToAdd);
                for (int i = start + deleteCount; i < size; i++) {
                    newList.add(thisArray.get(i));
                }
                // update original array
                List<Object> originalList = thisArray.toList();
                originalList.clear();
                originalList.addAll(newList);
                return removedElements;
            }
        });
        prototype.put("shift", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                if (size == 0) {
                    return Undefined.INSTANCE;
                }
                Object firstElement = thisArray.get(0);
                List<Object> newList = new ArrayList<>(size - 1);
                for (int i = 1; i < size; i++) {
                    newList.add(thisArray.get(i));
                }
                // update original array
                List<Object> originalList = thisArray.toList();
                originalList.clear();
                originalList.addAll(newList);
                return firstElement;
            }
        });
        prototype.put("unshift", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                if (args.length == 0) {
                    return thisArray.size();
                }
                List<Object> newList = new ArrayList<>(thisArray.size() + args.length);
                newList.addAll(Arrays.asList(args));
                for (int i = 0; i < thisArray.size(); i++) {
                    newList.add(thisArray.get(i));
                }
                // update original array
                List<Object> originalList = thisArray.toList();
                originalList.clear();
                originalList.addAll(newList);
                return originalList.size();
            }
        });
        prototype.put("lastIndexOf", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                if (size == 0) {
                    return -1;
                }
                if (args.length == 0) {
                    return -1;
                }
                Object searchElement = args[0];
                int fromIndex = size - 1;
                if (args.length > 1 && args[1] != null) {
                    Number n = Terms.toNumber(args[1]);
                    if (Double.isNaN(n.doubleValue())) {
                        fromIndex = size - 1;
                    } else {
                        fromIndex = n.intValue();
                        if (fromIndex < 0) {
                            fromIndex = size + fromIndex;
                        } else if (fromIndex >= size) {
                            fromIndex = size - 1;
                        }
                    }
                }
                if (fromIndex < 0) {
                    return -1;
                }
                for (int i = fromIndex; i >= 0; i--) {
                    if (Terms.eq(thisArray.get(i), searchElement, false)) {
                        return i;
                    }
                }
                return -1;
            }
        });
        prototype.put("pop", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                if (size == 0) {
                    return Undefined.INSTANCE;
                }
                Object lastElement = thisArray.get(size - 1);
                List<Object> newList = new ArrayList<>(size - 1);
                for (int i = 0; i < size - 1; i++) {
                    newList.add(thisArray.get(i));
                }
                // update original array
                List<Object> originalList = thisArray.toList();
                originalList.clear();
                originalList.addAll(newList);
                return lastElement;
            }
        });
        prototype.put("at", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                if (size == 0 || args.length == 0 || args[0] == null) {
                    return Undefined.INSTANCE;
                }
                int index = Terms.toNumber(args[0]).intValue();
                if (index < 0) {
                    index = size + index;
                }
                if (index < 0 || index >= size) {
                    return Undefined.INSTANCE;
                }
                return thisArray.get(index);
            }
        });
        prototype.put("copyWithin", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                if (size == 0 || args.length == 0) {
                    return thisArray.toList();
                }
                int target = Terms.toNumber(args[0]).intValue();
                if (target < 0) {
                    target = Math.max(size + target, 0);
                }
                int start = 0;
                if (args.length > 1 && args[1] != null) {
                    start = Terms.toNumber(args[1]).intValue();
                    if (start < 0) {
                        start = Math.max(size + start, 0);
                    }
                }
                int end = size;
                if (args.length > 2 && args[2] != null) {
                    end = Terms.toNumber(args[2]).intValue();
                    if (end < 0) {
                        end = Math.max(size + end, 0);
                    }
                }
                start = Math.min(start, size);
                end = Math.min(end, size);
                target = Math.min(target, size);
                // The elements to be copied
                List<Object> toCopy = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    toCopy.add(thisArray.get(i));
                }
                if (toCopy.isEmpty()) {
                    return thisArray.toList();
                }
                // Create a copy of the list to avoid concurrent modification issues
                List<Object> list = new ArrayList<>(thisArray.toList());
                // Copy elements over
                int copyCount = 0;
                for (int i = target; i < size && copyCount < toCopy.size(); i++) {
                    list.set(i, toCopy.get(copyCount++));
                }
                // Update the original array
                List<Object> originalList = thisArray.toList();
                originalList.clear();
                originalList.addAll(list);
                return thisArray.toList();
            }
        });
        prototype.put("keys", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                List<Object> result = new ArrayList<>();
                int size = thisArray.size();
                for (int i = 0; i < size; i++) {
                    result.add(i);
                }
                return result;
            }
        });
        prototype.put("values", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                return thisArray.toList();
            }
        });
        prototype.put("entries", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                List<Object> result = new ArrayList<>();
                int size = thisArray.size();
                for (int i = 0; i < size; i++) {
                    List<Object> entry = new ArrayList<>();
                    entry.add(i);
                    entry.add(thisArray.get(i));
                    result.add(entry);
                }
                return result;
            }
        });
        prototype.put("findLast", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                if (size == 0 || args.length == 0) {
                    return Undefined.INSTANCE;
                }
                Invokable invokable = toInvokable(args[0]);
                for (int i = size - 1; i >= 0; i--) {
                    Object value = thisArray.get(i);
                    Object result = invokable.invoke(value, i, thisArray);
                    if (Terms.isTruthy(result)) {
                        return value;
                    }
                }
                return Undefined.INSTANCE;
            }
        });
        prototype.put("findLastIndex", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                if (size == 0 || args.length == 0) {
                    return -1;
                }
                Invokable invokable = toInvokable(args[0]);
                for (int i = size - 1; i >= 0; i--) {
                    Object value = thisArray.get(i);
                    Object result = invokable.invoke(value, i, thisArray);
                    if (Terms.isTruthy(result)) {
                        return i;
                    }
                }
                return -1;
            }
        });
        prototype.put("with", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                int size = thisArray.size();
                if (size == 0 || args.length < 2) {
                    return thisArray.toList();
                }
                int index = Terms.toNumber(args[0]).intValue();
                if (index < 0) {
                    index = size + index;
                }
                if (index < 0 || index >= size) {
                    return thisArray.toList(); // If index is out of bounds, return a copy of the array
                }
                Object value = args[1];
                // Create a copy of the original array
                List<Object> result = new ArrayList<>(thisArray.toList());
                // Replace the value at the specified index
                result.set(index, value);
                return result;
            }
        });
        prototype.put("group", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ArrayLike thisArray = thisArray(array, thisObject);
                if (args.length == 0) {
                    return new JsObject();
                }
                Invokable callbackFn = toInvokable(args[0]);
                Map<String, List<Object>> groups = new HashMap<>();
                for (KeyValue kv : toIterable(thisArray)) {
                    Object key = callbackFn.invoke(kv.value, kv.index, thisArray);
                    String keyStr = key == null ? "null" : key.toString();
                    if (!groups.containsKey(keyStr)) {
                        groups.put(keyStr, new ArrayList<>());
                    }
                    groups.get(keyStr).add(kv.value);
                }
                JsObject result = new JsObject();
                for (Map.Entry<String, List<Object>> entry : groups.entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
                return result;
            }
        });
        return prototype;
    }

    @SuppressWarnings("unchecked")
    private static void flattenArray(List<Object> source, List<Object> result, int depth) {
        for (Object item : source) {
            if (depth > 0 && (item instanceof List || item instanceof ArrayLike)) {
                List<Object> nestedList;
                if (item instanceof ArrayLike) {
                    nestedList = ((ArrayLike) item).toList();
                } else {
                    nestedList = (List<Object>) item;
                }
                flattenArray(nestedList, result, depth - 1);
            } else {
                result.add(item);
            }
        }
    }

    static Map<String, Object> getBaseObjectPrototype(JsObject object) {
        Map<String, Object> prototype = new HashMap<>();
        prototype.put("toString", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                return TO_STRING(thisObject == null ? object : thisObject);
            }
        });
        prototype.put("constructor", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
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
        prototype.put("toString", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                return TO_STRING(thisObject == null ? function : thisObject);
            }
        });
        prototype.put("constructor", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                return function.invoke(args);
            }

            @Override
            public boolean equals(Object obj) {
                return function.equals(obj);
            }
        });
        prototype.put("call", (Invokable) args -> {
            ShiftArgs shifted = new ShiftArgs(args);
            function.thisObject = shifted.thisObject;
            return function.invoke(shifted.args);
        });
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
    };

    static Invokable PARSE_INT = args -> Terms.toNumber(args[0]);

}
