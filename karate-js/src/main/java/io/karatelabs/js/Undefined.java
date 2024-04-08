package io.karatelabs.js;

public class Undefined {

    public static final Undefined INSTANCE = new Undefined();

    private Undefined() {
        // singleton
    }

    @Override
    public String toString() {
        return "undefined";
    }

}
