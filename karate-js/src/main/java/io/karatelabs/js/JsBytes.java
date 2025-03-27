package io.karatelabs.js;

public class JsBytes extends JsObject {

    final byte[] bytes;

    public JsBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    Prototype getChildPrototype() {
        return new Prototype() {
            @Override
            public Object get(String prototypeKey) {
                switch (prototypeKey) {
                    case "length":
                        return new Property(() -> bytes.length);
                }
                return null;
            }
        };
    }

}
