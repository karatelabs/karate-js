package io.karatelabs.js;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class EngineRunner {

    static final Logger logger = LoggerFactory.getLogger(EngineRunner.class);

    @Test
    void test01() {
        File file = new File("src/test/resources/test-temp.js");
        Engine engine = new Engine();
        Object result = engine.eval(file);
        System.out.println("result:\n" + result);
    }

}
