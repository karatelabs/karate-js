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

public class JavaClass implements Constructable, JavaMethods, JavaFields {

    private final Class<?> clazz;

    public JavaClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    public JavaClass(String className) {
        try {
            clazz = Class.forName(className);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object construct(Object[] args) {
        return JavaUtils.construct(clazz, args);
    }

    @Override
    public Object call(String name, Object[] args) {
        return JavaUtils.invoke(clazz, name, args);
    }

    @Override
    public Object read(String name) {
        return JavaUtils.get(clazz, name);
    }

}
