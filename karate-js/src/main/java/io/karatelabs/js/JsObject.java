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

public class JsObject extends Prototype implements ObjectLike, Invokable {

    Object thisObject;
    private final Map<String, Object> map;

    public JsObject(Map<String, Object> map) {
        this.map = map;
    }

    public JsObject() {
        this(new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    @Override
    Map<String, Object> initPrototype() {
        Map<String, Object> prototype = new HashMap<>();
        prototype.put("toString", (Invokable) args -> TO_STRING(thisObject == null ? this : thisObject));
        prototype.put("hasOwnProperty", (Invokable) args -> {
            if (args.length == 0) {
                return false;
            }
            Object target = thisObject == null ? this : thisObject;
            String prop = args[0].toString();
            if (target instanceof JsObject) {
                JsObject jsObj = (JsObject) target;
                return jsObj.toMap().containsKey(prop);
            } else if (target instanceof ObjectLike) {
                ObjectLike objLike = (ObjectLike) target;
                Collection<String> keys = objLike.keys();
                return keys.contains(prop);
            } else if (target instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) target;
                return map.containsKey(prop);
            }
            return false;
        });
        // static ======================================================================================================
        prototype.put("keys", (Invokable) args -> {
            List<Object> result = new ArrayList<>();
            for (KeyValue kv : JsArray.toIterable(args[0])) {
                result.add(kv.key);
            }
            return result;
        });
        prototype.put("values", (Invokable) args -> {
            List<Object> result = new ArrayList<>();
            for (KeyValue kv : JsArray.toIterable(args[0])) {
                result.add(kv.value);
            }
            return result;
        });
        prototype.put("entries", (Invokable) args -> {
            List<Object> result = new ArrayList<>();
            for (KeyValue kv : JsArray.toIterable(args[0])) {
                List<Object> entry = new ArrayList<>();
                entry.add(kv.key);
                entry.add(kv.value);
                result.add(entry);
            }
            return result;
        });
        prototype.put("assign", (Invokable) args -> {
            if (args.length < 1) {
                return Undefined.INSTANCE;
            }
            Object target = args[0];
            if (target == null) {
                throw new RuntimeException("cannot convert undefined or null to object");
            }
            if (!(target instanceof JsObject) && !(target instanceof Map)) {
                target = new JsObject();
            }
            for (int i = 1; i < args.length; i++) {
                Object source = args[i];
                if (source == null) {
                    continue; // Skip null/undefined sources
                }
                Iterable<KeyValue> properties = JsArray.toIterable(source);
                if (properties != null) {
                    for (KeyValue kv : properties) {
                        if (target instanceof JsObject) {
                            ((JsObject) target).put(kv.key, kv.value);
                        } else {
                            ((Map) target).put(kv.key, kv.value);
                        }
                    }
                }
            }
            return target;
        });
        prototype.put("fromEntries", (Invokable) args -> {
            if (args.length < 1 || args[0] == null) {
                throw new RuntimeException("cannot convert undefined or null to object");
            }
            Object entriesObj = args[0];
            JsObject result = new JsObject();
            Iterable<KeyValue> entries = JsArray.toIterable(entriesObj);
            if (entries == null) {
                return result;
            }
            for (KeyValue kv : entries) {
                Object entry = kv.value;
                if (entry == null) {
                    continue;
                }
                String key;
                Object value;
                if (entry instanceof List) {
                    List<Object> entryList = (List<Object>) entry;
                    if (entryList.size() >= 2) {
                        key = entryList.get(0).toString();
                        value = entryList.get(1);
                        result.put(key, value);
                    }
                } else if (entry instanceof ArrayLike) {
                    ArrayLike entryArray = (ArrayLike) entry;
                    if (entryArray.size() >= 2) {
                        key = entryArray.get(0).toString();
                        value = entryArray.get(1);
                        result.put(key, value);
                    }
                }
            }
            return result;
        });
        prototype.put("is", (Invokable) args -> {
            if (args.length < 2) {
                return false;
            }
            return Terms.eq(args[0], args[1], true);
        });
        return prototype;
    }

    @Override
    public Object get(String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        }
        if ("prototype".equals(name)) {
            return getPrototype();
        }
        Object result = getPrototype().get(name);
        if (result instanceof Property) {
            return ((Property) result).get();
        }
        return result;
    }

    @Override
    public void put(String name, Object value) {
        map.put(name, value);
    }

    @Override
    public boolean hasKey(String name) {
        if (map.containsKey(name)) {
            return true;
        }
        return getPrototype().containsKey(name);
    }

    @Override
    public Collection<String> keys() {
        return map.keySet();
    }

    @Override
    public void remove(String name) {
        map.remove(name);
    }

    @Override
    public Map<String, Object> toMap() {
        return map;
    }

    @Override
    public Object invoke(Object... args) {
        return new JsObject(); // todo string, number, date
    }

}
