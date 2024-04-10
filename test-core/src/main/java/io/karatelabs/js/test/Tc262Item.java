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
package io.karatelabs.js.test;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tc262Item {

    public final File file;
    public final String source;
    public final Map<String, Object> metadata;
    public final String skipReason;
    public final List<File> preFiles;

    public Tc262Item(File ecmaDir, File file) {
        this.file = file;
        source = Tc262Utils.toString(file);
        metadata = getMetadata(source);
        skipReason = getSkipReason(file, metadata);
        preFiles = getPreFiles(ecmaDir, metadata);
    }

    public static Map<String, Object> getMetadata(String text) {
        int start = text.indexOf("/*---");
        if (start == -1) {
            return null;
        }
        int end = text.indexOf("---*/");
        if (end == -1) {
            return null;
        }
        String raw = text.substring(start + 5, end);
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        Object value = yaml.load(raw);
        String json = JsonUtils.toJson(value);
        return JsonUtils.fromJson(json);
    }

    private static final String TEST_PREFIX = "/test262/test/";

    static final List<String> IGNORED_FILES = List.of(
            "harness/detachArrayBuffer.js", // buffer
            "harness/assert-throws-same-realm.js", // js realms
            "harness/deepEqual-primitives.js", // symbols
            "harness/assert-throws-native.js", // built-in errors
            "harness/nans.js", // karate fails, to investigate
            "harness/compare-array-arraylike.js", // rhino fails
            "harness/compare-array-arguments.js" // rhino fails
    );

    static final List<String> IGNORED_FLAGS = List.of(
            "async",
            "async-functions"
    );

    static final List<String> IGNORED_FEATURES = List.of(
            "TypedArray",
            "Temporal",
            "async-functions",
            "generators",
            "BigInt", // good to have
            "Symbol"
    );

    static final List<String> IGNORED_INCLUDES = List.of(
            "propertyHelper.js", // object properties
            "promiseHelper.js",
            "asyncHelpers.js",
            "timer.js",
            "fnGlobalObject.js",
            "nativeFunctionMatcher.js",
            "detachArrayBuffer.js",
            "deepEqual.js", // uses symbols and unusual types
            "assertRelativeDateMs.js",
            "proxyTrapsHelper.js"
    );

    @SuppressWarnings("unchecked")
    public static String getSkipReason(File file, Map<String, Object> metadata) {
        String fileName = file.getPath();
        for (String name : IGNORED_FILES) {
            if (fileName.endsWith(TEST_PREFIX + name)) {
                return "ignore";
            }
        }
        if (metadata != null) {
            List<String> flags = (List<String>) metadata.get("flags");
            if (flags != null) {
                for (String flag : flags) {
                    if (IGNORED_FLAGS.contains(flag)) {
                        return "flag:" + flag;
                    }
                }
            }
            List<String> features = (List<String>) metadata.get("features");
            if (features != null) {
                for (String feature : features) {
                    if (IGNORED_FEATURES.contains(feature)) {
                        return "feature:" + feature;
                    }
                }
            }
            List<String> includes = (List<String>) metadata.get("includes");
            if (includes != null) {
                for (String include : includes) {
                    if (IGNORED_INCLUDES.contains(include)) {
                        return "include:" + include;
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<File> getPreFiles(File ecmaDir, Map<String, Object> metadata) {
        List<File> files = new ArrayList<>();
        if (metadata == null) {
            return files;
        }
        List<String> includes = (List<String>) metadata.get("includes");
        if (includes == null) {
            return files;
        }
        for (String pre : includes) {
            files.add(new File(ecmaDir.getPath() + "/harness/" + pre));
        }
        return files;
    }

}
