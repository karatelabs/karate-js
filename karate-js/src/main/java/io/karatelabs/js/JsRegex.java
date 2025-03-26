/*
 * The MIT License
 *
 * Copyright 2025 Karate Labs Inc.
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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class JsRegex extends JsObject implements Invokable {

    final String pattern;
    final String flags;
    final Pattern compiledPattern;
    final boolean global;

    int lastIndex = 0;

    public JsRegex() {
        this("(?:)");
    }

    public JsRegex(String literalText) {
        if (literalText.startsWith("/")) {
            int lastSlashIndex = literalText.lastIndexOf('/');
            if (lastSlashIndex <= 0) {
                throw new EvalError("Invalid RegExp literal: " + literalText);
            }
            // extract pattern and flags from the literal
            this.pattern = literalText.substring(1, lastSlashIndex);
            this.flags = lastSlashIndex < literalText.length() - 1
                    ? literalText.substring(lastSlashIndex + 1)
                    : "";
        } else {
            // string patterns without delimiters
            this.pattern = literalText;
            this.flags = "";
        }
        this.global = this.flags.contains("g");
        int javaFlags = translateJsFlags(this.flags);
        try {
            // unescape js-specific regex syntax that differs from Java
            String javaPattern = translateJsRegexToJava(this.pattern);
            this.compiledPattern = Pattern.compile(javaPattern, javaFlags);
        } catch (PatternSyntaxException e) {
            throw new EvalError("invalid regex: " + pattern + " - " + e.getMessage());
        }
    }

    public JsRegex(String pattern, String flags) {
        this.pattern = pattern;
        this.flags = flags != null ? flags : "";
        this.global = this.flags.contains("g");
        int javaFlags = translateJsFlags(this.flags);
        try {
            String javaPattern = translateJsRegexToJava(this.pattern);
            this.compiledPattern = Pattern.compile(javaPattern, javaFlags);
        } catch (PatternSyntaxException e) {
            throw new EvalError("invalid regex: " + pattern + " - " + e.getMessage());
        }
    }

    private static String translateJsRegexToJava(String jsPattern) {
        // handle any JavaScript regex syntax that needs special handling in Java
        // examples might include certain escape sequences or character classes
        return jsPattern;
    }

    private static int translateJsFlags(String flags) {
        int javaFlags = 0;
        if (flags.contains("i")) {
            javaFlags |= Pattern.CASE_INSENSITIVE;
        }
        if (flags.contains("m")) {
            javaFlags |= Pattern.MULTILINE;
        }
        // The "s" flag (dotall) makes "." match newline characters
        if (flags.contains("s")) {
            javaFlags |= Pattern.DOTALL;
        }
        return javaFlags;
    }

    public boolean test(String str) {
        if (global) {
            // for global, start at lastIndex
            Matcher matcher = compiledPattern.matcher(str);
            boolean found = matcher.find(lastIndex);
            if (found) {
                lastIndex = matcher.end();
            } else {
                lastIndex = 0;
            }
            return found;
        } else {
            // non-global regex does not update lastIndex
            return compiledPattern.matcher(str).find();
        }
    }

    public Object exec(String str) {
        Matcher matcher = compiledPattern.matcher(str);
        boolean found;
        if (global) {
            // for global, start at lastIndex
            found = matcher.find(lastIndex);
            if (found) {
                lastIndex = matcher.end();
            } else {
                lastIndex = 0;
                return null;
            }
        } else {
            // non-global regex always starts from beginning
            found = matcher.find();
        }
        if (!found) {
            return null;
        }
        // create result array with match and capture groups
        List<Object> matches = new ArrayList<>();
        matches.add(matcher.group(0)); // Full match
        // add capture groups
        for (int i = 1; i <= matcher.groupCount(); i++) {
            String group = matcher.group(i);
            matches.add(group != null ? group : ""); // js returns "" for undefined groups, not null
        }
        return new JsArray(matches) {
            @Override
            Map<String, Object> initPrototype() {
                Map<String, Object> prototype = super.initPrototype();
                prototype.put("index", new Property(matcher::start));
                prototype.put("input", new Property(() -> str));
                return prototype;
            }
        };
    }

    @Override
    public String toString() {
        return "/" + pattern + "/" + flags;
    }

    @Override
    Map<String, Object> initPrototype() {
        Map<String, Object> prototype = super.initPrototype();
        prototype.put("test", (Invokable) args -> {
            if (args.length == 0 || args[0] == null) {
                return false;
            }
            JsRegex regex = this;
            if (thisObject instanceof JsRegex) {
                regex = (JsRegex) thisObject;
            }
            return regex.test(args[0].toString());
        });
        prototype.put("exec", (Invokable) args -> {
            if (args.length == 0 || args[0] == null) {
                return null;
            }
            JsRegex regex = this;
            if (thisObject instanceof JsRegex) {
                regex = (JsRegex) thisObject;
            }
            return regex.exec(args[0].toString());
        });
        prototype.put("source", new Property(() -> pattern));
        prototype.put("flags", new Property(() -> flags));
        prototype.put("lastIndex", new Property(() -> lastIndex));
        prototype.put("global", new Property(() -> global));
        prototype.put("ignoreCase", new Property(() -> flags.contains("i")));
        prototype.put("multiline", new Property(() -> flags.contains("m")));
        prototype.put("dotAll", new Property(() -> flags.contains("s")));
        prototype.put("toString", (Invokable) args -> toString());
        return prototype;
    }

    @Override
    public Object invoke(Object... args) {
        if (args.length == 0) {
            return new JsRegex(); // empty regex in JS
        }
        String patternStr = args[0].toString();
        String flagsStr = args.length > 1 ? args[1].toString() : "";
        return new JsRegex(patternStr, flagsStr);
    }

}
