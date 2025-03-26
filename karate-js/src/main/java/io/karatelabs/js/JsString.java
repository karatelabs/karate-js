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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class JsString extends JsFunction {

    final String text;

    public JsString(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    Map<String, Object> initPrototype() {
        Map<String, Object> prototype = super.initPrototype();
        prototype.put("indexOf", (Invokable) args -> {
            if (args.length > 1) {
                return text.indexOf((String) args[0], ((Number) args[1]).intValue());
            }
            return text.indexOf((String) args[0]);
        });
        prototype.put("startsWith", (Invokable) args -> {
            if (args.length > 1) {
                return text.startsWith((String) args[0], ((Number) args[1]).intValue());
            }
            return text.startsWith((String) args[0]);
        });
        prototype.put("length", new Property(text::length));
        prototype.put("replaceAll", (Invokable) args -> text.replaceAll((String) args[0], (String) args[1]));
        prototype.put("getBytes", (Invokable) args -> text.getBytes(StandardCharsets.UTF_8));
        prototype.put("split", (Invokable) args -> Arrays.asList(text.split((String) args[0])));
        prototype.put("charAt", (Invokable) args -> {
            int index = ((Number) args[0]).intValue();
            if (index < 0 || index >= text.length()) {
                return "";
            }
            return String.valueOf(text.charAt(index));
        });
        prototype.put("charCodeAt", (Invokable) args -> {
            int index = ((Number) args[0]).intValue();
            if (index < 0 || index >= text.length()) {
                return Undefined.NAN;
            }
            return (int) text.charAt(index);
        });
        prototype.put("codePointAt", (Invokable) args -> {
            int index = ((Number) args[0]).intValue();
            if (index < 0 || index >= text.length()) {
                return Undefined.INSTANCE;
            }
            return text.codePointAt(index);
        });
        prototype.put("concat", (Invokable) args -> {
            StringBuilder sb = new StringBuilder(text);
            for (Object arg : args) {
                sb.append(arg);
            }
            return sb.toString();
        });
        prototype.put("endsWith", (Invokable) args -> {
            if (args.length > 1) {
                int endPosition = ((Number) args[1]).intValue();
                return text.substring(0, Math.min(endPosition, text.length())).endsWith((String) args[0]);
            }
            return text.endsWith((String) args[0]);
        });
        prototype.put("includes", (Invokable) args -> {
            String searchString = (String) args[0];
            if (args.length > 1) {
                int position = ((Number) args[1]).intValue();
                return text.indexOf(searchString, position) >= 0;
            }
            return text.contains(searchString);
        });
        prototype.put("lastIndexOf", (Invokable) args -> {
            if (args.length > 1) {
                return text.lastIndexOf((String) args[0], ((Number) args[1]).intValue());
            }
            return text.lastIndexOf((String) args[0]);
        });
        prototype.put("padEnd", (Invokable) args -> {
            int targetLength = ((Number) args[0]).intValue();
            String padString = args.length > 1 ? (String) args[1] : " ";
            if (padString.isEmpty()) {
                padString = " ";
            }
            if (text.length() >= targetLength) {
                return text;
            }
            int padLength = targetLength - text.length();
            StringBuilder sb = new StringBuilder(text);
            for (int i = 0; i < padLength; i++) {
                sb.append(padString.charAt(i % padString.length()));
            }
            return sb.toString();
        });
        prototype.put("padStart", (Invokable) args -> {
            int targetLength = ((Number) args[0]).intValue();
            String padString = args.length > 1 ? (String) args[1] : " ";
            if (padString.isEmpty()) {
                padString = " ";
            }
            if (text.length() >= targetLength) {
                return text;
            }
            int padLength = targetLength - text.length();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < padLength; i++) {
                sb.append(padString.charAt(i % padString.length()));
            }
            sb.append(text);
            return sb.toString();
        });
        prototype.put("repeat", (Invokable) args -> {
            int count = ((Number) args[0]).intValue();
            if (count < 0) {
                throw new RuntimeException("Invalid count value");
            }
            return text.repeat(count);
        });
        prototype.put("replace", (Invokable) args -> {
            // simple implementation - doesn't handle regex
            return text.replace((String) args[0], (String) args[1]);
        });
        prototype.put("slice", (Invokable) args -> {
            int beginIndex = ((Number) args[0]).intValue();
            int endIndex = args.length > 1 ? ((Number) args[1]).intValue() : text.length();
            // handle negative indices
            if (beginIndex < 0) beginIndex = Math.max(text.length() + beginIndex, 0);
            if (endIndex < 0) endIndex = Math.max(text.length() + endIndex, 0);
            // ensure proper range
            beginIndex = Math.min(beginIndex, text.length());
            endIndex = Math.min(endIndex, text.length());
            if (beginIndex >= endIndex) return "";
            return text.substring(beginIndex, endIndex);
        });
        prototype.put("substring", (Invokable) args -> {
            int beginIndex = ((Number) args[0]).intValue();
            int endIndex = args.length > 1 ? ((Number) args[1]).intValue() : text.length();
            // ensure indices within bounds
            beginIndex = Math.min(Math.max(beginIndex, 0), text.length());
            endIndex = Math.min(Math.max(endIndex, 0), text.length());
            // swap if beginIndex > endIndex (per JS spec)
            if (beginIndex > endIndex) {
                int temp = beginIndex;
                beginIndex = endIndex;
                endIndex = temp;
            }
            return text.substring(beginIndex, endIndex);
        });
        prototype.put("toLowerCase", (Invokable) args -> text.toLowerCase());
        prototype.put("toUpperCase", (Invokable) args -> text.toUpperCase());
        prototype.put("trim", (Invokable) args -> text.trim());
        prototype.put("trimEnd", (Invokable) args -> text.replaceAll("\\s+$", ""));
        prototype.put("trimStart", (Invokable) args -> text.replaceAll("^\\s+", ""));
        // aliases as per JS spec
        prototype.put("trimRight", (Invokable) args -> text.replaceAll("\\s+$", ""));
        prototype.put("trimLeft", (Invokable) args -> text.replaceAll("^\\s+", ""));
        prototype.put("valueOf", (Invokable) args -> text);
        prototype.put("fromCharCode", (Invokable) args -> {
            StringBuilder sb = new StringBuilder();
            for (Object arg : args) {
                if (arg instanceof Number) {
                    Number num = (Number) arg;
                    sb.append((char) num.intValue());
                }
            }
            return sb.toString();
        });
        prototype.put("fromCodePoint", (Invokable) args -> {
            StringBuilder sb = new StringBuilder();
            for (Object arg : args) {
                if (arg instanceof Number) {
                    Number num = (Number) arg;
                    int n = num.intValue();
                    if (n < 0 || n > 0x10FFFF) {
                        throw new RuntimeException("invalid code point: " + num);
                    }
                    sb.appendCodePoint(n);
                }
            }
            return sb.toString();
        });
        return prototype;
    }

    @Override
    public Object invoke(Object... args) {
        String temp = "";
        if (args.length > 0 && args[0] != null) {
            temp = args[0].toString();
        }
        return new JsString(temp);
    }

}
