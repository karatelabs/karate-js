package io.karatelabs.js;

public class JsBytes extends JsObject {

    final byte[] bytes;

    public JsBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    Prototype initPrototype() {
        Prototype wrapped = super.initPrototype();
        return new Prototype(wrapped) {
            @Override
            public Object getProperty(String propName) {
                switch (propName) {
                    case "length":
                        return bytes.length;
                }
                return null;
            }
        };
    }

}
