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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Context {

    static final Logger logger = LoggerFactory.getLogger(Context.class);

    public static final Context EMPTY = new Context(null, Collections.emptyMap(), null);

    private final Context parent;
    private final Context caller;
    private final Map<String, Object> bindings;

    BiConsumer<Node, Exception> onError;
    BiConsumer<String, Object> onAssign;
    boolean ignoreErrors;
    int errorCount;
    int statementCount;

    private Context(Context parent, Map<String, Object> bindings, Context caller) {
        this.parent = parent;
        this.bindings = bindings;
        this.caller = caller;
    }

    private static Map<String, Object> globals() {
        Map<String, Object> globals = new HashMap<>();
        globals.put("Java", JsCommon.JAVA_GLOBAL);
        globals.put("undefined", Undefined.INSTANCE);
        globals.put("Object", JsCommon.GLOBAL_OBJECT);
        globals.put("Array", JsCommon.GLOBAL_ARRAY);
        globals.put("Error", new JsError("Error"));
        globals.put("TypeError", new JsError("TypeError"));
        globals.put("String", JsCommon.GLOBAL_STRING);
        globals.put("Infinity", Terms.POSITIVE_INFINITY);
        globals.put("NaN", Undefined.NAN);
        globals.put("Math", JsCommon.MATH);
        globals.put("parseInt", JsCommon.PARSE_INT);
        globals.put("JSON", JsCommon.JSON);
        globals.put("console", JsCommon.createConsole(System.out::println));
        return globals;
    }

    public void setOnConsole(Consumer<String> onConsole) {
        parent.bindings.put("console", JsCommon.createConsole(onConsole));
    }

    public void setOnError(BiConsumer<Node, Exception> onError) {
        this.onError = onError;
    }

    public void setOnAssign(BiConsumer<String, Object> onAssign) {
        this.onAssign = onAssign;
    }

    public void setIgnoreErrors(boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getStatementCount() {
        return statementCount;
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    void setParent(String key, Object value) {
        parent.bindings.put(key, value);
    }

    public static Context root() {
        Context root = new Context(null, globals(), null);
        return new Context(root);
    }

    Context(Context parent) {
        this(parent, new HashMap<>(), null);
    }

    Context merge(Context caller) {
        return new Context(this, new HashMap<>(), caller);
    }

    Context copy() {
        Map<String, Object> map = new HashMap<>(bindings);
        return new Context(null, map, null);
    }

    public Object get(String name) {
        if (bindings.containsKey(name)) {
            return bindings.get(name);
        }
        if (caller != null && caller.hasKey(name)) {
            return caller.get(name);
        }
        if (parent != null && parent.hasKey(name)) {
            return parent.get(name);
        }
        return Undefined.INSTANCE;
    }

    public boolean hasKey(String name) {
        if (bindings.containsKey(name)) {
            return true;
        }
        if (caller != null && caller.hasKey(name)) {
            return true;
        }
        return parent != null && parent.hasKey(name);
    }

    public void declare(String name, Object value) {
        if (value instanceof JsFunction && !"this".equals(name)) {
            ((JsFunction) value).setName(name);
        }
        bindings.put(name, value);
    }

    public void update(String name, Object value) {
        if (bindings.containsKey(name)) {
            bindings.put(name, value);
        } else if (caller != null && caller.hasKey(name)) {
            caller.update(name, value);
        } else if (parent != null && parent.hasKey(name)) {
            parent.update(name, value);
        } else {
            bindings.put(name, value);
            if (onAssign != null) {
                onAssign.accept(name, value);
            }
        }
    }

    public void remove(String name) {
        bindings.remove(name);
    }

    //==================================================================================================================
    //
    boolean construct;
    Node currentNode;

    private boolean stopped;
    private Object returnValue;
    private Object errorThrown;

    public Node getCurrentNode() {
        return currentNode;
    }

    Object stopAndThrow(Object error) {
        stopped = true;
        errorThrown = error;
        if (logger.isTraceEnabled()) {
            String info;
            if (error instanceof JsObject) {
                info = error + " " + ((JsObject) error).toMap();
            } else {
                info = error + "";
            }
            logger.trace("**ERROR** {}", info);
            if (currentNode != null) {
                logger.trace(currentNode.toStringError(""));
            }
        }
        return error;
    }

    Object stopAndReturn(Object value) {
        stopped = true;
        returnValue = value;
        errorThrown = null;
        return value;
    }

    boolean isStopped() {
        return stopped;
    }

    boolean isError() {
        return errorThrown != null;
    }

    public Object getReturnValue() {
        return errorThrown == null ? returnValue : null;
    }

    public Object getErrorThrown() {
        return errorThrown;
    }

    void updateFrom(Context childContext) {
        stopped = childContext.stopped;
        errorThrown = childContext.errorThrown;
        returnValue = childContext.returnValue;
    }

}
