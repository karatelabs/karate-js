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

import java.io.File;
import java.util.Map;

public class Engine {

    public static JavaBridge JAVA_BRIDGE = new JavaBridge() {
        // non-final default that you can over-ride
    };

    private boolean convertUndefined = true;

    public void setConvertUndefined(boolean convertUndefined) {
        this.convertUndefined = convertUndefined;
    }

    public static boolean DEBUG = false;

    public final Context context;
    public Source source;

    private Engine(Context context) {
        this.context = context;
    }

    public Engine() {
        this(Context.root());
    }

    public Object eval(Source source) {
        return evalInternal(source);
    }

    public Object eval(File file) {
        return evalInternal(Source.of(file));
    }

    public Object eval(String text) {
        return evalInternal(Source.of(text));
    }

    public Object evalWith(String text, Map<String, Object> vars) {
        return evalInternal(Source.of(text), vars);
    }

    public static boolean isUndefined(Object o) {
        return o == Undefined.INSTANCE || Undefined.NAN.equals(o);
    }

    private Object evalInternal(Source source) {
        return evalInternal(source, null);
    }

    private Object evalInternal(Source source, Map<String, Object> localVars) {
        this.source = source;
        try {
            Parser parser = new Parser(source);
            Node node = parser.parse();
            Context evalContext;
            if (localVars == null) {
                evalContext = context;
            } else {
                evalContext = new Context(context);
                evalContext.getBindings().putAll(localVars);
            }
            Object result = Interpreter.eval(node, evalContext);
            if (isUndefined(result) && convertUndefined) {
                return null;
            }
            return result;
        } catch (Throwable e) {
            String message = e.getMessage();
            if (message == null) {
                message = e + "";
            }
            message = message + "\n" + source.getStringForLog();
            throw new RuntimeException(message);
        }
    }

    public void setRootBinding(String name, Object value) {
        context.setParent(name, value);
    }

    public void set(String name, Object value) {
        context.declare(name, value);
    }

    public Object get(String name) {
        Object value = context.get(name);
        if (isUndefined(value) && convertUndefined) {
            return null;
        }
        return value;
    }

    public Engine copy() {
        return new Engine(context.copy());
    }

    public static Object exec(File file) {
        Engine engine = new Engine();
        return engine.eval(file);
    }

    public static Object exec(String text) {
        Engine engine = new Engine();
        return engine.eval(text);
    }

}
