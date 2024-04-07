# Karate JS
Lightweight JavaScript engine for the JVM.

## Summary

There are very few options for embedding a JS engine in Java and the most logical choice, [Graal JS](https://github.com/oracle/graaljs) does not fully meet the needs of our users. Graal JS is very capable, but simply does not allow JS to be used by concurrent threads. Many Graal users have requested for this in a [GitHub issue](https://github.com/oracle/graaljs/issues/481) opened in July 2021.

We incorporated Karate Labs to build a principled company, not driven by market valuation. In line with our commitment of remaining true to the origin of Karate and principles of open source, we are releasing a JS engine implemented in Java from scratch.
 
This project also includes a performance benchmark comparing the new engine's performance with Rhino, Nashorn and Graal.

## Background
Evaluating JavaScript within Java (or any JVM language) at run-time has many uses. The most common are to:

* Enable end-users to customize some logic at run-time via scripting.
* Allow end-users to "glue" existing Java code or libraries into such scripts.

Being able to do this without compiling code or having to know anything about Java or build-tooling is a big win for users.

Options are few. The Mozilla [Rhino](https://github.com/mozilla/rhino) engine which used to be embedded in JDK 6 and 7, continues to be available as an open-source project, but is quite old. Then came [Nashorn](https://www.oracle.com/technical-resources/articles/java/jf14-nashorn.html) which was included in JDK 8 (GA in 2014).

Things looked great until Oracle suddenly announced that [Nashorn would be deprecated](https://www.infoq.com/news/2018/06/deprecate-nashorn/). Teams had to move to Graal.

The [Karate](https://github.com/karatelabs/karate) project was using Nashorn extensively and deeply depended on it. Migrating to Graal was not easy and resulted in [breaking changes for users](https://github.com/karatelabs/karate/wiki/1.0-upgrade-guide#breaking-changes-due-to-js-engine-change).

# The Problem
Graal JS is great for the most part, but there is one issue which [continues to trouble us](https://github.com/search?q=repo%3Akaratelabs%2Fkarate+%22Multi+threaded+access%22&type=issues). Graal simply does not allow JS to be used by concurrent threads. Many users of Graal have requested for this in a [GitHub issue](https://github.com/oracle/graaljs/issues/481) opened in July 2021. But the response from the Graal team has been very clear. 

[To quote](https://github.com/oracle/graaljs/issues/481#issuecomment-1106094406):

> The right body to address this criticism to is the ECMAScript TC39, in charge of the ECMAScript/JavaScript specification. JavaScript, by design, is specified with single-threaded evaluation semantics. Unlike other languages, it is NOT designed to allow full multi-threading.

This is very unfortunate since Nashorn supported multi-threading without any issues. For a project like Karate where running workloads in parallel is a core differentiator, this is a [very severe limitation](https://github.com/karatelabs/karate/issues/2222). We even had to tell users to [avoid using JavaScript in some situations](https://github.com/karatelabs/karate#java-function-references).

## Our Opinion
The request to the Graal team that someone raised in the open [GitHub issue](https://github.com/oracle/graaljs/issues/481) is perfectly reasonable. Provide a configuration setting or flag that teams can use "at their own risk". But for whatever reason, the Graal team is not open to making this change. 

The discussion is about running JS within a JVM, and we suspect that most teams do not care about "pure Node JS or ECMA compatibility". What we *really* want is to be able to mix Java and JS custom code in novel ways on the JVM. It would be a shame to waste Java's ability to run concurrent and async code.

Two other areas where we are not happy with Graal is the [sheer size of the dependencies](#dependency-size) (more than 80 MB !) and the fact that they recently started providing [two different versions of the artifacts with different licenses](https://github.com/oracle/graaljs#maven-artifact).

Projects like Quarkus and Spring Boot have support for GraalVM and we are seeing more instances of users [running into library conflicts](https://github.com/karatelabs/karate/issues/2536). Graal also seems to introduce [more CVEs than what you would expect](https://github.com/karatelabs/karate/issues/2148) because of possibly its large surface area - and we have had to scramble to release upgrades multiple times. One particular upgrade [forced us to require Java 17](https://github.com/karatelabs/karate/issues/2401) before we felt our users were really ready for it.

Finally, there are clear signs that the Graal team prioritizes the use of the GraalVM instead of a "stock JVM" which is not something we are keen to do just yet. The supposed performance difference is also a concern. Quoting from the [documentation](https://github.com/oracle/graaljs/blob/master/docs/user/RunOnJDK.md):

> As GraalVM JavaScript is a Java application, it is possible to execute it on a stock Java VM like OpenJDK. When executed without the Graal Compiler, JavaScript performance will be significantly worse. While the JIT compilers available on stock JVMs can execute and JIT-compile the GraalVM JavaScript codebase, they cannot optimize it to its full performance potential.

There was a time when we thought Rhino - and then Nashorn was the perfect solution for JS on the JVM. They were even built into the JDK. If the Graal situation changes drastically in the future, we don't want to go through another painful migration and inflict more breaking changes on users.

# The Alternative
We decided that attempting to create a new JS engine in Java from scratch would be easier than trying to convince the JavaScript governing body to change the way that JavaScript works.

## Initial Version
In the spirit of open-source - we are releasing the work we have done so far. It is not complete, and we feel that we don't have to implement 100% of the spec in order to get the value that we need.

The good thing is that it already has solved some of the hardest problems - the parser, support for all arithmetic operators, precedence and expressions. Arrow functions and prototype chains are supported.

To get a sense of what is already supported, take a look at the [main unit-test](test-core/src/test/java/io/karatelabs/js/EvalTest.java).

## Highlights
* Simple code base, easy to understand and contribute
* Zero runtime dependencies (except SLF4J)
* Less than 100 KB as a JAR
* Runs on Java 11 and up
* High performance [JFlex](https://jflex.de) generated [lexer](karate-js/src/main/jflex/js.flex)
* Hand-written [parser](karate-js/src/main/java/io/karatelabs/js/Parser.java) for the best performance
* [Initial benchmarking](#benchmark) indicates that Karate-JS is much faster than Rhino, Nashorn and Graal
* Project includes utilities to run the [Official ECMAScript Conformance Test Suite](https://github.com/tc39/test262) (TC262).

## Features we plan to *not* support

We are far from 100% compliance, but we may never need to be. Of course, we will gladly welcome contributions in case there is interest in supporting some of the below. But making Karate-JS 100% ECMA conformant or Node JS compatible is currently NOT the objective of this project. If you have feedback, feel free to open an issue and start a discussion.

In other words, our immediate goal is to support the most common things that Karate users do with JS. The next step for us is to replace Graal-JS in Karate, and the large suite of regression-tests and demos we have will guide us.

Here is a list of what we *don't* plan to support:

* Symbols
* Labels
* Buffers and other "exotic" primitive datatypes beyond `string`, `number` and `boolean`
* `with` statement
* Concept of "strict mode" or not-strict mode
* `async`, `await`, `yield` or promises
* Generator functions
* Regexes
* JS Date handling
* BigInt or decimals (users can delegate to the Java `BigDecimal` if needed)
* Modules, `import`, `require` etc.
* Node-like APIs e.g. `process.env` – users can also delegate to Java instead
* Accessor properties (get / set)
* Internationalization
* Things like `setTimeout()`

## Benchmark
A [GitHub action](.github/workflows/benchmark.yml) is available that runs a [performance benchmark](test-perf/src/main/java/io/karatelabs/js/benchmark/Main.java). The benchmark is in 2 parts

* A Java interop demo that calls methods and bean-properties on Java classes and objects. This is run 100 times.
* A set of 35 tests from the ECMA conformance suite that Karate-JS can support

The time taken to initialize the JS engine is also counted. You can run the benchmark on your machine after cloning this project by typing the following commands. The first command is to download the [ECMAScript Test Suite](https://github.com/tc39/test262) which will be partially used. Java 17 is required for Graal.

```
git clone --depth 1 https://github.com/tc39/test262.git ../test262
mvn clean install
mvn -f test-perf/pom.xml package -P fatjar
java -jar test-perf/target/benchmark.jar 
```

Here are the results from a sample run taken from GitHub actions (OpenJDK 17, [Ubuntu 2204](https://github.com/actions/runner-images/blob/ubuntu22/20240324.2/images/ubuntu/Ubuntu2204-Readme.md)). All time-durations reported are in milliseconds.

### Java Interop
In the current version, Karate-JS is doing brute-force reflection to call constructors amd methods on Java classes and objects. There is room for future optimization - but already Karate-JS seems to be almost 3 times faster.

> Rhino is not included in this test because it supports Java interop in a very different way.
```
====== java interop, iterations: 100
karate: 676.425176
nashorn: 1436.384098
graal: 1874.445704
```

### Selected TC262 Tests

Below is a summary of 35 tests picked out of the [TC262 suite](https://github.com/tc39/test262) (that Karate-JS can run as of now).

> Nashorn is not included in this test because it fails on some of them, but we haven't spent time on investigating the cause.

Time durations reported are in milliseconds.

```
====== ecma tc262 test suite
karate: 150.465456
graal: 1257.738839
rhino: 1033.519628
```

Karate-JS seems to be significantly faster, by almost 10 times.

Click below to expand the full list. Each test requires 2 "harness" scripts and one or more "include" scripts to be evaluated as a pre-requisite. These are non-trivial examples.

<details>
<summary>Selected TC262 tests</summary>
<code>
{karate=31.346476, graal=153.42322, rhino=203.938031} ../test262/test/harness/byteConversionValues.js
{karate=10.404436, graal=102.730443, rhino=43.454042} ../test262/test/harness/compare-array-same-elements-different-order.js
{karate=1.380339, graal=24.910275, rhino=36.32686} ../test262/test/harness/assert-throws-no-error.js
{karate=1.260335, graal=46.623505, rhino=34.82269} ../test262/test/harness/assert-throws-no-arg.js
{karate=6.5113, graal=29.918356, rhino=23.442804} ../test262/test/harness/compare-array-different-elements.js
{karate=1.162093, graal=26.952822, rhino=12.905361} ../test262/test/harness/sta.js
{karate=1.284922, graal=22.234956, rhino=14.096732} ../test262/test/harness/assert-notsamevalue-nan.js
{karate=2.892012, graal=27.844384, rhino=20.672626} ../test262/test/harness/compare-array-falsy-arguments.js
{karate=1.497407, graal=18.549832, rhino=14.13164} ../test262/test/harness/assert-throws-null.js
{karate=1.644141, graal=23.946783, rhino=13.121041} ../test262/test/harness/assert-throws-incorrect-ctor.js
{karate=10.733911, graal=67.751932, rhino=41.508719} ../test262/test/harness/decimalToHexString.js
{karate=2.071781, graal=23.1043, rhino=14.723381} ../test262/test/harness/assert-throws-custom-typeerror.js
{karate=2.305647, graal=21.783431, rhino=16.546349} ../test262/test/harness/compare-array-different-length.js
{karate=1.578118, graal=16.636708, rhino=11.69767} ../test262/test/harness/assert-notsamevalue-zeros.js
{karate=2.66137, graal=31.764918, rhino=19.469539} ../test262/test/harness/proxytrapshelper-overrides.js
{karate=2.324731, graal=20.688917, rhino=14.756707} ../test262/test/harness/compare-array-samevalue.js
{karate=1.194873, graal=22.010736, rhino=11.011961} ../test262/test/harness/assert-throws-single-arg.js
{karate=1.761691, graal=20.140233, rhino=27.157449} ../test262/test/harness/compare-array-empty.js
{karate=1.769394, graal=29.388898, rhino=20.233155} ../test262/test/harness/assert-throws-null-fn.js
{karate=3.296267, graal=26.54696, rhino=10.2788} ../test262/test/harness/assert-notsamevalue-notsame.js
{karate=1.138327, graal=13.256363, rhino=10.375722} ../test262/test/harness/tcoHelper.js
{karate=1.224388, graal=13.846154, rhino=10.298367} ../test262/test/harness/assert-throws-custom.js
{karate=2.194729, graal=18.097066, rhino=21.57815} ../test262/test/harness/proxytrapshelper-default.js
{karate=5.263274, graal=26.347448, rhino=16.792359} ../test262/test/harness/dateConstants.js
{karate=4.678449, graal=45.385019, rhino=23.172637} ../test262/test/harness/compare-array-sparse.js
{karate=2.489019, graal=40.616983, rhino=20.212067} ../test262/test/harness/assert-samevalue-zeros.js
{karate=1.085547, graal=29.805906, rhino=17.882695} ../test262/test/harness/assert-false.js
{karate=1.698903, graal=26.992804, rhino=14.97767} ../test262/test/harness/assert-samevalue-objects.js
{karate=1.625638, graal=22.25891, rhino=11.640254} ../test262/test/harness/assert-true.js
{karate=1.001353, graal=24.000473, rhino=14.904502} ../test262/test/harness/assert-samevalue-nan.js
{karate=1.335304, graal=17.763893, rhino=8.534512} ../test262/test/harness/assert-throws-primitive.js
{karate=1.188551, graal=12.356011, rhino=7.720842} ../test262/test/harness/assert-obj.js
{karate=0.969882, graal=12.829482, rhino=16.073557} ../test262/test/harness/assert-notsamevalue-objects.js
{karate=2.402838, graal=25.108444, rhino=16.303757} ../test262/test/harness/compare-array-same-elements-same-order.js
{karate=1.741534, graal=18.699054, rhino=14.818949} ../test262/test/harness/assert-samevalue-same.js
</code>
</details>

### Dependency Size

Karate-JS weighs in at less than 100 KB. It should stay in the same range even after supporting more JS syntax in the future.

For comparison, here are the dependencies that Graal-JS needs - which add up to a whopping 87 MB.

```
% du -sch *.jar
120K	collections-23.1.0.jar
 38M	icu4j-23.1.0.jar
272K	jniutils-23.1.0.jar
 27M	js-language-23.1.0.jar
 76K	js-scriptengine-23.1.0.jar
216K	nativeimage-23.1.0.jar
916K	polyglot-23.1.0.jar
3.2M	regex-23.1.0.jar
 16M	truffle-api-23.1.0.jar
 60K	truffle-compiler-23.1.0.jar
1.0M	truffle-runtime-23.1.0.jar
 24K	word-23.1.0.jar
 87M	total
```

The above was generated by running `mvn package dependency:copy-dependencies` and then looking at the `target/dependency` folder for a simple `pom.xml` with the minimal dependencies [recommended by the Graal JS team](https://github.com/oracle/graaljs/blob/master/docs/user/RunOnJDK.md).