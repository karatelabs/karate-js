package io.karatelabs.js;

@FunctionalInterface
public interface JsCallable {

    Object call(Object thisObject, Object... args);

}
