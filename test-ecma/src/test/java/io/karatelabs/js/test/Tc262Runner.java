package io.karatelabs.js.test;

import io.karatelabs.js.Engine;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.stream.Stream;

class Tc262Runner {

    static final Logger logger = LoggerFactory.getLogger(Tc262Runner.class);

    private static Engine init(File ecmaDir) {
        Engine engine = new Engine();
        engine.eval(new File(ecmaDir.getPath() + "/harness/assert.js"));
        engine.eval(new File(ecmaDir.getPath() + "/harness/sta.js"));
        return engine;
    }

    @TestFactory
    Stream<DynamicTest> testEcma() {
        File ecmaDir = new File("../../test262");
        return Tc262Utils.getTestFiles(ecmaDir, "harness")
                .map(item -> DynamicTest.dynamicTest(item.file.getName(), () -> {
                    Engine engine = init(ecmaDir);
                    for (File preFile : item.preFiles) {
                        try {
                            engine.eval(preFile);
                        } catch (Exception e) {
                            logger.debug("{} - {}", item.file, e.getMessage());
                        }
                    }
                    engine.eval(item.source);
                }));
    }


}
