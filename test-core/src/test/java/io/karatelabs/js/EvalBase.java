package io.karatelabs.js;

import io.karatelabs.js.test.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

class EvalBase {

    static final Logger logger = LoggerFactory.getLogger(EvalTest.class);

    Context context;

    Object eval(String text) {
        return eval(text, null);
    }

    Object eval(String text, String vars) {
        Parser parser = new Parser(Source.of(text));
        Node node = parser.parse();
        context = Context.root();
        if (vars != null) {
            Map<String, Object> map = JsonUtils.fromJson(vars);
            map.forEach((k, v) -> context.declare(k, v));
        }
        return Interpreter.eval(node, context);
    }

    void matchEval(String text, String expected) {
        matchEval(text, expected, null);
    }

    void matchEval(String text, String expected, String vars) {
        match(eval(text, vars), expected);
    }

    void match(Object actual, String expected) {
        NodeUtils.match(actual, expected);
    }

    Object get(String varName) {
        return context.get(varName);
    }

}
