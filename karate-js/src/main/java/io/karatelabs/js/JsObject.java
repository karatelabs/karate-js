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

public class JsObject implements ObjectLike, Invokable, Iterable<KeyValue> {

    Object thisObject;
    private final Map<String, Object> map;

    public JsObject(Map<String, Object> map) {
        this.map = map;
    }

    public JsObject() {
        this(new HashMap<>());
    }

    private Prototype _prototype;

    final Prototype getPrototype() {
        if (_prototype == null) {
            _prototype = initPrototype();
        }
        return _prototype;
    }

    Prototype initPrototype() {
        return new Prototype(null) {
            @SuppressWarnings("unchecked")
            @Override
            public Object getProperty(String propName) {
                switch (propName) {
                    case "toString":
                        return (Invokable) args -> JsCommon.TO_STRING(thisObject == null ? this : thisObject);
                    case "hasOwnProperty":
                        return (Invokable) args -> {
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
                                Object value = objLike.get(prop);
                                return value != null;
                            } else if (target instanceof Map) {
                                Map<String, Object> map = (Map<String, Object>) target;
                                return map.containsKey(prop);
                            }
                            return false;
                        };
                    case "keys":
                        return (Invokable) args -> {
                            List<Object> result = new ArrayList<>();
                            for (KeyValue kv : JsArray.toIterable(args[0])) {
                                result.add(kv.key);
                            }
                            return result;
                        };
                    case "values":
                        return (Invokable) args -> {
                            List<Object> result = new ArrayList<>();
                            for (KeyValue kv : JsArray.toIterable(args[0])) {
                                result.add(kv.value);
                            }
                            return result;
                        };
                    case "entries":
                        return (Invokable) args -> {
                            List<Object> result = new ArrayList<>();
                            for (KeyValue kv : JsArray.toIterable(args[0])) {
                                List<Object> entry = new ArrayList<>();
                                entry.add(kv.key);
                                entry.add(kv.value);
                                result.add(entry);
                            }
                            return result;
                        };
                    // static ==========================================================================================
                    case "assign":
                        return (Invokable) args -> {
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
                        };
                    case "fromEntries":
                        return (Invokable) args -> {
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
                                } else if (entry instanceof JsArray) {
                                    JsArray entryArray = (JsArray) entry;
                                    if (entryArray.size() >= 2) {
                                        key = entryArray.get(0).toString();
                                        value = entryArray.get(1);
                                        result.put(key, value);
                                    }
                                }
                            }
                            return result;
                        };
                    case "is":
                        return (Invokable) args -> {
                            if (args.length < 2) {
                                return false;
                            }
                            return Terms.eq(args[0], args[1], true);
                        };
                }
                return null;
            }
        };
    }

    @Override
    public Object get(String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        }
        if ("prototype".equals(name)) {
            return getPrototype();
        }
        return getPrototype().get(name);
    }

    @Override
    public void put(String name, Object value) {
        map.put(name, value);
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

    @Override
    public Iterator<KeyValue> iterator() {
        return toIterable(thisObject).iterator();
    }

    @SuppressWarnings("unchecked")
    static Iterable<KeyValue> toIterable(Object object) {
        if (object instanceof List) {
            object = new JsArray((List<Object>) object);
        }
        if (object instanceof JsArray) {
            final JsArray array = (JsArray) object;
            return () -> {
                final int size = array.size();
                return new Iterator<>() {
                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < size;
                    }

                    @Override
                    public KeyValue next() {
                        int i = index++;
                        return new KeyValue(array, i, i + "", array.get(i));
                    }
                };
            };
        }
        if (object instanceof Map) {
            object = new JsObject((Map<String, Object>) object);
        }
        if (object instanceof ObjectLike) {
            final ObjectLike objectLike = (ObjectLike) object;
            return () -> {
                final Iterator<Map.Entry<String, Object>> entries = objectLike.toMap().entrySet().iterator();
                return new Iterator<>() {
                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return entries.hasNext();
                    }

                    @Override
                    public KeyValue next() {
                        Map.Entry<String, Object> entry = entries.next();
                        return new KeyValue(objectLike, index++, entry.getKey(), entry.getValue());
                    }
                };
            };
        }
        return null;
    }

}
