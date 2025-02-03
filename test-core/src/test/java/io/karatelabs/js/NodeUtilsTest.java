package io.karatelabs.js;

import org.junit.jupiter.api.Test;

class NodeUtilsTest {

    @Test
    void testConversion() {
        Node node = new Node(Type.EXPR);
        Node c1 = new Node(Type.LIT_EXPR);
        node.children.add(c1);
        String text = "1";
        Chunk chunk = new Chunk(Source.of(""), Token.NUMBER, 0,0, 0, text);
        Node c2 = new Node(chunk);
        c1.children.add(c2);
        NodeUtils.assertEquals(text, node, "1");
    }

}
