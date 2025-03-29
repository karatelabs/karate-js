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

public class JsArray extends JsObject {

    final List<Object> list;

    public JsArray(List<Object> list) {
        this.list = list;
    }

    public JsArray() {
        this(new ArrayList<>());
    }

    Prototype initPrototype() {
        Prototype wrapped = super.initPrototype();
        return new Prototype(wrapped) {
            @SuppressWarnings("unchecked")
            @Override
            public Object getProperty(String propName) {
                switch (propName) {
                    case "length":
                        return list.size();
                    case "map":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                List<Object> results = new ArrayList<>();
                                Invokable invokable = toInvokable(args);
                                for (KeyValue kv : this) {
                                    Object result = invokable.invoke(kv.value, kv.index);
                                    results.add(result);
                                }
                                return results;
                            }
                        };
                    case "filter":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                List<Object> results = new ArrayList<>();
                                Invokable invokable = toInvokable(args);
                                for (KeyValue kv : this) {
                                    Object result = invokable.invoke(kv.value, kv.index);
                                    if (Terms.isTruthy(result)) {
                                        results.add(kv.value);
                                    }
                                }
                                return results;
                            }
                        };
                    case "join":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                StringBuilder sb = new StringBuilder();
                                String delimiter;
                                if (args.length > 0 && args[0] != null) {
                                    delimiter = args[0].toString();
                                } else {
                                    delimiter = ",";
                                }
                                for (KeyValue kv : this) {
                                    if (sb.length() != 0) {
                                        sb.append(delimiter);
                                    }
                                    sb.append(kv.value);
                                }
                                return sb.toString();
                            }
                        };
                    case "find":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                Invokable invokable = toInvokable(args);
                                for (KeyValue kv : this) {
                                    Object result = invokable.invoke(kv.value, kv.index);
                                    if (Terms.isTruthy(result)) {
                                        return kv.value;
                                    }
                                }
                                return Undefined.INSTANCE;
                            }
                        };
                    case "findIndex":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                Invokable invokable = toInvokable(args);
                                for (KeyValue kv : this) {
                                    Object result = invokable.invoke(kv.value, kv.index);
                                    if (Terms.isTruthy(result)) {
                                        return kv.index;
                                    }
                                }
                                return -1;
                            }
                        };
                    case "push":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                for (Object o : args) {
                                    thisArray.add(o);
                                }
                                return thisArray.size();
                            }
                        };
                    case "reverse":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                int size = thisArray.size();
                                List<Object> result = new ArrayList<>();
                                for (int i = size; i > 0; i--) {
                                    result.add(thisArray.get(i - 1));
                                }
                                return result;
                            }
                        };
                    case "includes":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                for (KeyValue kv : this) {
                                    if (Terms.eq(kv.value, args[0], false)) {
                                        return true;
                                    }
                                }
                                return false;
                            }
                        };
                    case "indexOf":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "slice":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "forEach":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                Invokable invokable = toInvokable(args);
                                for (KeyValue kv : this) {
                                    invokable.invoke(kv.value, kv.index, thisObject);
                                }
                                return Undefined.INSTANCE;
                            }
                        };
                    case "concat":
                        return new JsFunction() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                List<Object> result = new ArrayList<>(thisArray.toList());
                                for (Object arg : args) {
                                    if (arg instanceof List) {
                                        result.addAll((List<Object>) arg);
                                    } else if (arg instanceof JsArray) {
                                        result.addAll(((JsArray) arg).toList());
                                    } else {
                                        result.add(arg);
                                    }
                                }
                                return result;
                            }
                        };
                    case "every":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                if (thisArray.size() == 0) {
                                    return true;
                                }
                                Invokable invokable = toInvokable(args);
                                for (KeyValue kv : this) {
                                    Object result = invokable.invoke(kv.value, kv.index, thisArray);
                                    if (!Terms.isTruthy(result)) {
                                        return false;
                                    }
                                }
                                return true;
                            }
                        };
                    case "some":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                if (thisArray.size() == 0) {
                                    return false;
                                }
                                Invokable invokable = toInvokable(args);
                                for (KeyValue kv : this) {
                                    Object result = invokable.invoke(kv.value, kv.index, thisArray);
                                    if (Terms.isTruthy(result)) {
                                        return true;
                                    }
                                }
                                return false;
                            }
                        };
                    case "reduce":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                Invokable callback = toInvokable(args);
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
                        };
                    case "reduceRight":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                Invokable callback = toInvokable(args);
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
                        };
                    case "flat":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                int depth = 1;
                                if (args.length > 0 && args[0] != null) {
                                    Number depthNum = Terms.toNumber(args[0]);
                                    if (!Double.isNaN(depthNum.doubleValue()) && !Double.isInfinite(depthNum.doubleValue())) {
                                        depth = depthNum.intValue();
                                    }
                                }
                                List<Object> result = new ArrayList<>();
                                flatten(thisArray.toList(), result, depth);
                                return result;
                            }
                        };
                    case "flatMap":
                        return new JsFunction() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                Invokable callback = toInvokable(args);
                                if (callback == null) {
                                    throw new RuntimeException("flatMap() requires a callback function");
                                }
                                List<Object> mappedResult = new ArrayList<>();
                                int index = 0;
                                for (Object item : thisArray.toList()) {
                                    Object mapped = callback.invoke(item, index, thisArray);
                                    if (mapped instanceof List || mapped instanceof JsArray) {
                                        List<Object> nestedList;
                                        if (mapped instanceof JsArray) {
                                            nestedList = ((JsArray) mapped).toList();
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
                        };
                    case "sort":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "fill":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                if (args.length == 0) {
                                    return thisObject;
                                }
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "splice":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                if (args.length == 0) {
                                    return new ArrayList<>();
                                }
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "shift":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "unshift":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "lastIndexOf":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "pop":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "at":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "copyWithin":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "keys":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                List<Object> result = new ArrayList<>();
                                int size = thisArray.size();
                                for (int i = 0; i < size; i++) {
                                    result.add(i);
                                }
                                return result;
                            }
                        };
                    case "values":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                return thisArray.toList();
                            }
                        };
                    case "entries":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "findLast":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                int size = thisArray.size();
                                if (size == 0 || args.length == 0) {
                                    return Undefined.INSTANCE;
                                }
                                Invokable invokable = toInvokable(args);
                                for (int i = size - 1; i >= 0; i--) {
                                    Object value = thisArray.get(i);
                                    Object result = invokable.invoke(value, i, thisArray);
                                    if (Terms.isTruthy(result)) {
                                        return value;
                                    }
                                }
                                return Undefined.INSTANCE;
                            }
                        };
                    case "findLastIndex":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                int size = thisArray.size();
                                if (size == 0 || args.length == 0) {
                                    return -1;
                                }
                                Invokable invokable = toInvokable(args);
                                for (int i = size - 1; i >= 0; i--) {
                                    Object value = thisArray.get(i);
                                    Object result = invokable.invoke(value, i, thisArray);
                                    if (Terms.isTruthy(result)) {
                                        return i;
                                    }
                                }
                                return -1;
                            }
                        };
                    case "with":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
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
                        };
                    case "group":
                        return new JsFunction() {
                            @Override
                            public Object invoke(Object... args) {
                                JsArray thisArray = asArray(thisObject);
                                if (args.length == 0) {
                                    return new JsObject();
                                }
                                Invokable callbackFn = toInvokable(args);
                                Map<String, List<Object>> groups = new HashMap<>();
                                for (KeyValue kv : this) {
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
                        };
                    // static ==========================================================================================
                    case "from":
                        return (Invokable) args -> {
                            List<Object> results = new ArrayList<>();
                            Invokable invokable = null;
                            if (args.length > 1 && args[1] instanceof Invokable) {
                                invokable = (Invokable) args[1];
                            }
                            JsArray array = asArray(args[0]);
                            for (KeyValue kv : toIterable(array)) {
                                Object result = invokable == null ? kv.value : invokable.invoke(kv.value, kv.index);
                                results.add(result);
                            }
                            return results;
                        };
                    case "isArray":
                        return (Invokable) args -> args[0] instanceof List || args[0] instanceof JsArray;
                    case "of":
                        return (Invokable) Arrays::asList;
                }
                return null;
            }
        };
    }

    public Object get(int index) {
        return list.get(index);
    }

    public void set(int index, Object value) {
        list.set(index, value);
    }

    public void add(Object value) {
        list.add(value);
    }

    public void remove(int index) {
        list.remove(index);
    }

    public int size() {
        return list.size();
    }

    public List<Object> toList() {
        return list;
    }

    @SuppressWarnings("unchecked")
    static JsArray asArray(Object instance) {
        if (instance instanceof List) {
            return new JsArray((List<Object>) instance);
        } else if (instance instanceof JsArray) {
            return (JsArray) instance;
        } else if (instance instanceof Map) {
            return toArray((Map<String, Object>) instance);
        }
        throw new RuntimeException("not an array: " + instance);
    }

    static JsArray toArray(Map<String, Object> map) {
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

    static Invokable toInvokable(Object[] args) {
        if (args.length == 0) {
            throw new RuntimeException("function expected");
        }
        if (args[0] instanceof Invokable) {
            return (Invokable) args[0];
        }
        throw new RuntimeException("function expected");
    }

    @SuppressWarnings("unchecked")
    static void flatten(List<Object> source, List<Object> result, int depth) {
        for (Object item : source) {
            if (depth > 0 && (item instanceof List || item instanceof JsArray)) {
                List<Object> nestedList;
                if (item instanceof JsArray) {
                    nestedList = ((JsArray) item).toList();
                } else {
                    nestedList = (List<Object>) item;
                }
                flatten(nestedList, result, depth - 1);
            } else {
                result.add(item);
            }
        }
    }

}
