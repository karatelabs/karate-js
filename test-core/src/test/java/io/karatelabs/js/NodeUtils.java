package io.karatelabs.js;

import io.karatelabs.js.test.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class NodeUtils {

    static final Logger logger = LoggerFactory.getLogger(NodeUtils.class);

    public static void match(Object actual, String json) {
        Object expected = JsonUtils.fromJson(json);
        isEqualTo(actual, expected);
    }

    public static boolean isEqualTo(Object actual, Object expected) {
        try {
            if (expected instanceof List) {
                if (actual instanceof List) {
                    List<Object> actList = (List<Object>) actual;
                    List<Object> expList = (List<Object>) expected;
                    if (actList.size() != expList.size()) {
                        Assertions.fail("List size mismatch: " + actList.size() + " - " + expList.size());
                    }
                    int count = expList.size();
                    for (int i = 0; i < count; i++) {
                        Object actItem = actList.get(i);
                        Object expItem = expList.get(i);
                        boolean result = isEqualTo(actItem, expItem);
                        if (!result) {
                            Assertions.fail("List item mismatch at index " + i + ": " + actList.size() + " - " + expList.size());
                        }
                    }
                    return true;
                } else {
                    Assertions.fail("List expected, actual is: " + actual);
                }
            } else if (expected instanceof Map) {
                if (actual instanceof ObjectLike) {
                    actual = ((ObjectLike) actual).toMap();
                }
                if (actual instanceof Map) {
                    Map<String, Object> actMap = (Map<String, Object>) actual;
                    Map<String, Object> expMap = (Map<String, Object>) expected;
                    if (actMap.size() != expMap.size()) {
                        Assertions.fail("Map size mismatch: " + actMap.size() + " - " + expMap.size());
                    }
                    for (String key : expMap.keySet()) {
                        if (!actMap.containsKey(key)) {
                            Assertions.fail("Map does not contain key: " + key);
                        }
                        Object actValue = actMap.get(key);
                        Object expValue = expMap.get(key);
                        boolean result = isEqualTo(actValue, expValue);
                        if (!result) {
                            Assertions.fail("Map value mismatch for key " + key + ": " + actValue + " - " + expValue);
                        }
                    }
                    return true;
                } else {
                    Assertions.fail("Map expected, actual is: " + actual);
                }
            } else { // string or primitive
                if ("undefined".equals(expected)) {
                    expected = Undefined.INSTANCE;
//                } else if ("null".equals(expected)) {
//                    expected = null;
                }
                Assertions.assertEquals(expected, actual);
            }
        } catch (Throwable t) {
            logger.error("assertion failed:\nexpected:{}\nactual=>:{}", JsonUtils.toJson(expected), JsonUtils.toJson(actual));
            throw t;
        }
        return true;
    }

    public static Object ser(Node node) {
        switch (node.type) {
            case PAREN_EXPR:
                return ser(node.children.get(1));
            case OBJECT_ELEM:
                if (node.children.size() < 3) { // es6 enhanced object literals
                    return ser(node.children.get(0));
                } else {
                    return List.of(ser(node.children.get(0)) + ":", ser(node.children.get(2)));
                }
            case ARRAY_ELEM:
                return ser(node.children.get(0));
            case PROGRAM:
            case ROOT:
                // include key in serialized output
                String key = node.type.name();
                if (node.children.size() == 1) {
                    return Collections.singletonMap(key, ser(node.children.get(0)));
                } else {
                    List<Object> list = new ArrayList<>(node.children.size());
                    for (Node child : node.children) {
                        list.add(ser(child));
                    }
                    return Collections.singletonMap(key, list);
                }
            case _CHUNK:
                switch (node.chunk.token) {
                    case IDENT:
                        return "$" + node.chunk.text;
                    case S_STRING:
                    case D_STRING:
                    case NUMBER:
                    case NULL:
                    case TRUE:
                    case FALSE:
                        return Interpreter.eval(node, Context.EMPTY);
                    default:
                        return node.chunk.text;
                }
            default:
                if (node.children.size() == 1) {
                    return ser(node.children.get(0));
                } else {
                    List<Object> list = new ArrayList<>();
                    for (Node child : node.children) {
                        list.add(ser(child));
                    }
                    return list;
                }
        }
    }

    public static void assertEquals(String text, Node node, String string) {
        Object expected = JsonUtils.fromJson(string);
        Object actual = ser(node);
        String expectedJson = JsonUtils.toJson(expected);
        String actualJson = JsonUtils.toJson(actual);
        try {
            Assertions.assertEquals(expectedJson, actualJson);
        } catch (Throwable t) {
            // logger.debug("tree:\n{}", toString(node));
            logger.debug("text:\n{}", text);
            throw t;
        }
    }

    private static void spaces(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) {
            sb.append(' ');
        }
    }

    private static void recurse(StringBuilder sb, int level, Node node) {
        spaces(sb, level);
        sb.append(node).append('\n');
        for (Node child : node.children) {
            recurse(sb, level + 1, child);
        }
    }

    private static String toString(Node node) {
        StringBuilder sb = new StringBuilder();
        recurse(sb, 0, node);
        return sb.toString();
    }

}
