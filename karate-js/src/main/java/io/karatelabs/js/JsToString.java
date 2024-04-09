package io.karatelabs.js;

import java.util.List;

public class JsToString extends JsFunction {

    public JsToString(Object thisObject) {
        this.thisObject = thisObject;
    }

    @Override
    public Object invoke(Object... args) {
        if (thisObject == null) {
            return "[object Null]";
        }
        if (thisObject instanceof ArrayLike || thisObject instanceof List) {
            return "[object Array]";
        }
        if (thisObject instanceof String || thisObject instanceof Number || thisObject instanceof Boolean) {
            return thisObject.toString();
        }
        return "[object Object]";
    }

}
