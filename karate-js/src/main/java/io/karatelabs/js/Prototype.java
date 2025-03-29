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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

abstract class Prototype implements ObjectLike {

    private final Prototype wrapped;
    private Map<String, Object> props;

    Prototype(Prototype wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void put(String name, Object value) {
        if (props == null) {
            props = new HashMap<>();
        }
        props.put(name, value);
    }

    @Override
    public void remove(String name) {
        if (props != null) {
            props.remove(name);
        }
    }

    @Override
    public Map<String, Object> toMap() {
        return props == null ? Collections.emptyMap() : props;
    }

    @Override
    final public Object get(String name) {
        if (props != null && props.containsKey(name)) {
            return props.get(name);
        }
        Object result = getProperty(name);
        if (result != null) {
            return result;
        }
        if (wrapped != null) {
            return wrapped.get(name);
        }
        return null;
    }

    abstract Object getProperty(String key);

}
