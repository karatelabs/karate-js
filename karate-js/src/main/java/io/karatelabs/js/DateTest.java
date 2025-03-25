package io.karatelabs.js;

/**
 * Simple test class for the new JsDate implementation.
 * This is just for manual testing and can be deleted after verification.
 */
public class DateTest {
    
    public static void main(String[] args) {
        Engine engine = new Engine();
        
        // Test date construction
        Object result = engine.eval("var date = new Date(); date");
        System.out.println("New Date(): " + result);
        
        // Test timestamp constructor
        result = engine.eval("var fixedDate = new Date(1609459200000); fixedDate.getTime()");
        System.out.println("Timestamp constructor: " + result);
        
        // Test time operations
        result = engine.eval("var now = Date.now(); now");
        System.out.println("Date.now(): " + result);
        
        // Test date methods
        result = engine.eval("var d = new Date(1609459200000); d.getFullYear()");
        System.out.println("getFullYear(): " + result);
        
        result = engine.eval("var d = new Date(1609459200000); d.getMonth()");
        System.out.println("getMonth(): " + result);
        
        result = engine.eval("var d = new Date(1609459200000); d.getDate()");
        System.out.println("getDate(): " + result);
        
        // Test to string methods
        result = engine.eval("var d = new Date(1609459200000); d.toString()");
        System.out.println("toString(): " + result);
        
        result = engine.eval("var d = new Date(1609459200000); d.toISOString()");
        System.out.println("toISOString(): " + result);
        
        // Test functions using dates
        String js = "function getTimestamp(time) {\n"
                + "  if (time && time.getTime) {\n"
                + "    return time.getTime();\n"
                + "  }\n"
                + "  return time;\n"
                + "}\n"
                + "var d = new Date(1609459200000); getTimestamp(d)";
        result = engine.eval(js);
        System.out.println("Function with date: " + result);
    }
}
