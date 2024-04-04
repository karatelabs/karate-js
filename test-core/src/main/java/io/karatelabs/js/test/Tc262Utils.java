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

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

public class Tc262Utils {

    public static String toString(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<Tc262Item> getTestFiles(File ecmaDir, String testDir) {
        System.out.println("using dir: " + ecmaDir.getAbsolutePath());
        File testFile = new File(ecmaDir + File.separator + "test" + File.separator + testDir);
        if (!testFile.isDirectory()) {
            return Stream.empty();
        }
        FilenameFilter filter = (dir1, name) -> name.endsWith(".js");
        File[] fileArray = testFile.listFiles(filter);
        if (fileArray == null) {
            return Stream.empty();
        }
        return Arrays.stream(fileArray)
                .map(file -> new Tc262Item(ecmaDir, file))
                .filter(item -> item.skipReason == null);
    }

}
