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

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

public class NashornRunner implements Runner {

    private final long initTimeNanos;
    private final ScriptEngine engine;
    private final ScriptContext context;

    private long evalTimeNanos;
    private long bindTimeNanos;

    public NashornRunner() {
        Timer timer = new Timer();
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        context = new SimpleScriptContext();
        context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
        initTimeNanos = timer.elapsedTimeNanos();
    }

    @Override
    public String getName() {
        return "nashorn";
    }

    @Override
    public long getInitTimeNanos() {
        return initTimeNanos;
    }

    @Override
    public long getEvalTimeNanos() {
        return evalTimeNanos;
    }

    @Override
    public long getBindTimeNanos() {
        return bindTimeNanos;
    }

    @Override
    public Object eval(String js) {
        Timer timer = new Timer();
        try {
            return engine.eval(js, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            evalTimeNanos = timer.elapsedTimeNanos();
        }
    }

    @Override
    public void bind(String name, Object value) {
        Timer timer = new Timer();
        context.getBindings(ScriptContext.ENGINE_SCOPE).put(name, value);
        bindTimeNanos = timer.elapsedTimeNanos();
    }

}
