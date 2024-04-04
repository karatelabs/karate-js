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
package io.karatelabs.js.benchmark;

import java.util.ArrayList;
import java.util.List;

public class Scenario {

    public final String description;
    public final String script;
    public final Object expected;

    public Scenario(String description, String script, Object expected) {
        this.description = description;
        this.script = script;
        this.expected = expected;
    }

    public static final List<Scenario> scenarios = new ArrayList<>();

    static {
        scenarios.add(new Scenario(
                "java static method",
                "var DemoUtils = Java.type('io.karatelabs.js.benchmark.DemoUtils');"
                        + " DemoUtils.doWork()",
                "hello"));
        scenarios.add(new Scenario(
                "java instance method",
                "var DemoPojo = Java.type('io.karatelabs.js.benchmark.DemoPojo');"
                        + " var pojo = new DemoPojo(); pojo.doWork()",
                "hello"));
        scenarios.add(new Scenario(
                "java bean property",
                "var DemoPojo = Java.type('io.karatelabs.js.benchmark.DemoPojo');"
                        + " var pojo = new DemoPojo(); pojo.stringValue = 'hello'; pojo.stringValue",
                "hello"));
    }

}
