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

import java.util.List;
import java.util.function.Consumer;

public class JsCommon {

    static final JsObject GLOBAL_STRING = new JsString("");

    static final JsObject GLOBAL_REGEX = new JsRegex();

    static final JsDate GLOBAL_DATE = new JsDate();

    static final JsMath GLOBAL_MATH = new JsMath();

    static final JsObject JAVA_GLOBAL = createJavaGlobal();

    static final JsObject JSON = createJson();

    static final JsObject GLOBAL_OBJECT = new JsObject();

    static final JsArray GLOBAL_ARRAY = new JsArray();

    static Invokable PARSE_INT = args -> Terms.toNumber(args[0]);

    private static JsObject createJson() {
        JsObject object = new JsObject();
        object.put("stringify", (Invokable) args -> JSONValue.toJSONString(args[0]));
        object.put("parse", (Invokable) args -> JSONValue.parse((String) args[0]));
        return object;
    }

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

}
