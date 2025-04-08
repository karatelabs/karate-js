package io.karatelabs.js;

import java.util.Map;

public interface SimpleObject extends ObjectLike {

    @Override
    default void put(String name, Object value) {

    }

    @Override
    default void remove(String name) {

    }

    @Override
    default Map<String, Object> toMap() {
        return null;
    }

}
