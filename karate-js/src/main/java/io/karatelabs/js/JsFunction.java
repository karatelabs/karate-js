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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class JsFunction extends JsObject {

    private static final Object[] EMPTY = new Object[0];

    String name;
    Context invokeContext;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    Prototype initPrototype() {
        Prototype wrapped = super.initPrototype();
        return new Prototype(wrapped) {
            @Override
            public Object getProperty(String propName) {
                switch (propName) {
                    case "call":
                        return (Invokable) args -> {
                            ShiftArgs shifted = new ShiftArgs(args);
                            thisObject = shifted.thisObject;
                            return invoke(shifted.args);
                        };
                    case "constructor":
                        return JsFunction.this;
                    case "name":
                        return name;
                }
                return null;
            }
        };
    }

    private static class ShiftArgs {

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

}
