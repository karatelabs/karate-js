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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JsProperty {

    static final Logger logger = LoggerFactory.getLogger(JsProperty.class);

    final Node node;
    final Object object;
    final Context context;
    String name;
    Object index;

    JsProperty(Node node, Context context) {
        this.node = node;
        this.context = context;
        if (node.type == Type.EXPR) {
            node = node.children.get(0);
        }
        switch (node.type) {
            case REF_EXPR:
                object = null;
                name = node.getText();
                break;
            case REF_DOT_EXPR:
                Object temp;
                try {
                    // ignore any nested failures, the caller (interpreter) will check for java interop
                    // before bubbling up the "undefined" as an exception
                    temp = Interpreter.eval(node.children.get(0), context);
                } catch (Exception e) {
                    temp = Undefined.INSTANCE;
                }
                object = temp;
                name = node.children.get(2).getText();
                break;
            case REF_BRACKET_EXPR:
                object = Interpreter.eval(node.children.get(0), context);
                index = Interpreter.eval(node.children.get(2), context);
                name = null;
                break;
            case LIT_EXPR:
                object = Interpreter.eval(node.children.get(0), context);
                name = null;
                break;
            case PAREN_EXPR:
                object = Interpreter.eval(node.children.get(1), context);
                name = null;
                break;
            case FN_CALL_EXPR:
                object = Interpreter.eval(node, context); // evalFnCall
                name = null;
                break;
            default:
                throw new RuntimeException("cannot assign from: " + node);
        }
    }

    void set(Object value) {
        if (index instanceof Number) {
            Number num = (Number) index;
            if (object instanceof List) {
                ((List<Object>) object).set(num.intValue(), value);
            } else if (object instanceof JsArray) {
                ((JsArray) object).set(num.intValue(), value);
            } else {
                throw new RuntimeException("cannot set by index [" + index + "]:" + value + " on (non-array): " + object);
            }
        } else {
            if (index != null) {
                name = index + "";
            }
            if (name == null) {
                throw new RuntimeException("unexpected set [null]:" + value + " on: " + object);
            }
            if (value instanceof JsFunction) { // pre-process
                ((JsFunction) value).setName(name);
            }
            if (object == null) {
                context.update(name, value);
            } else if (object instanceof Map) {
                ((Map<String, Object>) object).put(name, value);
            } else if (object instanceof ObjectLike) {
                ((ObjectLike) object).put(name, value);
            } else if (object instanceof JavaClass) {
                ((JavaClass) object).update(name, value);
            } else {
                try {
                    Engine.JAVA_BRIDGE.set(object, name, value);
                } catch (Exception e) {
                    logger.error("java bridge error: {}", e.getMessage());
                    throw new RuntimeException("cannot set '" + name + "'");
                }
            }
        }
    }

    Object get() {
        return get(false);
    }

    private Object get(boolean function) {
        if (object == Undefined.INSTANCE) {
            return object;
        }
        if (index instanceof Number) {
            int num = ((Number) index).intValue();
            if (object instanceof List) {
                return ((List<Object>) object).get(num);
            }
            if (object instanceof JsArray) {
                return ((JsArray) object).get(num);
            }
            if (object instanceof String) {
                return ((String) object).substring(num, num + 1);
            }
            if (object instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) object;
                String key = num + "";
                if (map.containsKey(key)) {
                    return map.get(key);
                }
            }
            if (object instanceof ObjectLike) {
                ObjectLike map = (ObjectLike) object;
                String key = num + "";
                Object value = map.get(key);
                if (value != null) {
                    return value;
                }
            }
            throw new RuntimeException("get by index [" + index + "] for non-array: " + object);
        }
        if (index != null) {
            name = index + "";
        }
        if (name == null) {
            return object;
        }
        if (object instanceof List) {
            return (new JsArray((List<Object>) object).get(name));
        }
        if (object instanceof JsArray) {
            return ((JsArray) object).get(name);
        }
        if (object instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) object;
            if (map.containsKey(name)) { // performance optimization
                return map.get(name);
            }
            JsObject jsObject = new JsObject(map);
            Object result = jsObject.get(name);
            if (result != null) {
                return result;
            }
            // else attempt java interop
        }
        if (object instanceof ObjectLike) {
            return ((ObjectLike) object).get(name);
        }
        if (object instanceof String) {
            return new JsString((String) object).get(name);
        }
        if (object instanceof byte[]) {
            return new JsBytes((byte[]) object).get(name);
        }
        if (function && object instanceof JavaMethods) {
            return new JavaInvokable(name, (JavaMethods) object);
        }
        if (!function && object instanceof JavaFields) {
            return ((JavaFields) object).read(name);
        }
        if (object == null) { // literal or function reference (see constructor switch case)
            if (context.hasKey(name)) {
                return context.get(name);
            }
        }
        try {
            if (function) {
                if (object instanceof Class) {
                    return new JavaInvokable(name, new JavaClass((Class<?>) object));
                } else {
                    return new JavaInvokable(name, new JavaObject(object));
                }
            } else {
                if (object instanceof Class) {
                    return new JavaClass((Class<?>) object).read(name);
                } else {
                    return new JavaObject(object).get(name);
                }
            }
        } catch (Exception e) {
            return Undefined.INSTANCE;
        }
    }

    private String getErrorMessageExtra() {
        String message = "";
        if (name != null) {
            message = message + ", property: " + name;
        }
        if (object != null) {
            message = message + ", object: " + object;
        }
        return message;
    }

    Invokable getInvokable() {
        Object o = get(true);
        if (o instanceof Invokable) {
            return (Invokable) o;
        } else if (o instanceof Prototype) {
            Prototype prototype = (Prototype) o;
            return (Invokable) prototype.get("constructor");
        } else if (o instanceof JavaClass) {
            JavaClass jc = (JavaClass) o;
            return jc::construct;
        } else if (JavaFunction.isFunction(o)) {
            return new JavaFunction(o);
        } else if (o == Undefined.INSTANCE) {
            String className = node.getText();
            if (Engine.JAVA_BRIDGE.typeExists(className)) {
                try {
                    return args -> Engine.JAVA_BRIDGE.construct(className, args);
                } catch (Exception e) {
                    throw new RuntimeException("undefined is not a function" + getErrorMessageExtra());
                }
            } else {
                throw new RuntimeException("undefined is not a function" + getErrorMessageExtra());
            }
        } else if (o != null) { // java interop
            JavaObject jo = new JavaObject(o);
            return new JavaInvokable(name, jo);
        } else {
            throw new RuntimeException("null is not a function");
        }
    }

}

