package io.karatelabs.js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsDateTest extends EvalBase {

    @Test
    void testDev() {

    }

    @Test
    void testDateObject() {
        // Test date construction
        eval("var date = new Date()");
        assertInstanceOf(Object.class, get("date"));
        eval("var dateA = new Date(1609459200000); var timeA = dateA.getTime();");
        assertEquals(1609459200000L, get("timeA"));

        // Test static methods
        assertInstanceOf(Number.class, eval("Date.now()"));
        assertTrue((Long) eval("Date.now()") > 0);

        // Test parsing (exact value may vary by timezone)
        eval("var time = Date.parse('2021-01-01T00:00:00Z')");
        assertInstanceOf(Number.class, get("time"));

        eval("var fixedDate = new Date(1609459200000);" // 2021-01-01
                + "var time = fixedDate.getTime();"
                + "var year = fixedDate.getFullYear();"
                + "var month = fixedDate.getMonth();"
                + "var date = fixedDate.getDate();"
                + "var day = fixedDate.getDay();"
                + "var strDate = fixedDate.toString();"
                + "var isoDate = fixedDate.toISOString();");
        assertEquals(1609459200000L, get("time"));
        assertEquals(2021, get("year"));
        assertEquals(0, get("month"));
        assertEquals(1, get("date"));
        assertEquals(5, get("day"));
        assertNotNull(get("strDate"));
        assertNotNull(get("isoDate"));
        assertTrue(get("isoDate").toString().contains("2021-01-01"));

        // Test date string when called as function (without new)
        Object dateString = eval("Date()");
        assertInstanceOf(String.class, dateString);

        // Test ability to pass date object to a function that expects a timestamp
        String js = "function getTimestamp(time) {"
                + "    if (time && time.getTime) {"
                + "      return time.getTime();"
                + "    }"
                + "    return time;"
                + "};"
                + "var fixedDate = new Date(1609459200000);"
                + "var timestamp = getTimestamp(fixedDate)";
        eval(js);
        assertEquals(1609459200000L, get("timestamp"));
    }

}
