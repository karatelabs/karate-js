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

import io.karatelabs.js.test.Tc262Item;
import io.karatelabs.js.test.Tc262Utils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        List<Supplier<Runner>> suppliers = List.of(KarateRunner::new, NashornRunner::new, GraalRunner::new);
        Map<String, Double> totalEvalTimes = new LinkedHashMap<>();
        int javaIterations = 100;
        for (Supplier<Runner> supplier : suppliers) {
            Runner runner = supplier.get();
            String runnerName = runner.getName();
            double timeNanos = runner.getInitTimeNanos();
            for (int i = 0; i < javaIterations; i++) {
                for (Scenario scenario : Scenario.scenarios) {
                    Object result = runner.eval(scenario.script);
                    if (result == null || !result.equals(scenario.expected)) {
                        throw new RuntimeException("result mismatch: " + scenario.description
                                + ", " + scenario.expected + ":" + result);
                    }
                    timeNanos += runner.getEvalTimeNanos();
                }
            }
            Double totalTime = totalEvalTimes.get(runnerName);
            if (totalTime == null) {
                totalTime = timeNanos;
            }
            totalEvalTimes.put(runnerName, totalTime + timeNanos);
        }
        System.out.println("====== java interop, iterations: " + javaIterations);
        totalEvalTimes.forEach((k, v) -> {
            System.out.println(k + ": " + Timer.nanosToMillis(v));
        });
        System.out.println("======"); //================================================================================
        totalEvalTimes.clear();
        String ecmaRoot;
        if (args.length > 0) {
            ecmaRoot = args[0];
        } else {
            ecmaRoot = "../test262";
        }
        suppliers = List.of(KarateRunner::new, GraalRunner::new, RhinoRunner::new);
        List<Tc262Item> items = Tc262Utils.getTestFiles(new File(ecmaRoot), "harness").collect(Collectors.toList());
        for (Tc262Item item : items) {
            Map<String, Double> evalTimes = new LinkedHashMap<>();
            for (Supplier<Runner> supplier : suppliers) {
                try {
                    Runner runner = supplier.get();
                    String runnerName = runner.getName();
                    double timeNanos = runner.getInitTimeNanos();
                    runner.eval(Tc262Utils.toString(new File(ecmaRoot + "/harness/assert.js")));
                    timeNanos += runner.getEvalTimeNanos();
                    runner.eval(Tc262Utils.toString(new File(ecmaRoot + "/harness/sta.js")));
                    timeNanos += runner.getEvalTimeNanos();
                    for (File preFile : item.preFiles) {
                        runner.eval(Tc262Utils.toString(preFile));
                        timeNanos += runner.getEvalTimeNanos();
                    }
                    runner.eval(item.source);
                    timeNanos += runner.getEvalTimeNanos();
                    Double totalTime = totalEvalTimes.get(runnerName);
                    if (totalTime == null) {
                        totalTime = timeNanos;
                    }
                    totalEvalTimes.put(runnerName, totalTime + timeNanos);
                    evalTimes.put(runnerName, Timer.nanosToMillis(timeNanos));
                } catch (Exception e) {
                    System.err.println("failed: " + item.file);
                    throw e;
                }
            }
            System.out.println(evalTimes + " " + item.file);
        }
        System.out.println("====== ecma tc262 test suite");
        totalEvalTimes.forEach((k, v) -> {
            System.out.println(k + ": " + Timer.nanosToMillis(v));
        });

    }

}
