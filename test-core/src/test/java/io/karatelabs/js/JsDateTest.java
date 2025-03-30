package io.karatelabs.js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsDateTest extends EvalBase {

    @Test
    void testDateCreation() {
        // Test date creation with year, month, day parameters
        eval("var date = new Date(2025, 2, 15); var month = date.getMonth(); var day = date.getDate(); var year = date.getFullYear();");
        assertInstanceOf(Object.class, get("date"));
        assertEquals(2, get("month")); // March is month 2 in JavaScript (0-indexed)
        assertEquals(15, get("day"));
        assertEquals(2025, get("year"));
        // Test date creation with full parameters
        eval("var dateWithTime = new Date(2025, 2, 15, 13, 45, 30, 500);"
                + " var hours = dateWithTime.getHours(); var minutes = dateWithTime.getMinutes();"
                + " var seconds = dateWithTime.getSeconds(); var ms = dateWithTime.getMilliseconds()");
        assertEquals(13, get("hours"));
        assertEquals(45, get("minutes"));
        assertEquals(30, get("seconds"));
        assertEquals(500, get("ms"));
    }

    @Test
    void testDateSetters() {
        eval("var date = new Date(2025, 2, 15);"
                + "date.setDate(date.getDate() + 10);"
                + "var newDay = date.getDate();"
                + "date.setMonth(date.getMonth() + 1);"
                + "var newMonth = date.getMonth();"
                + "date.setHours(23, 59, 59, 999);"
                + "var endDayHours = date.getHours();"
                + "var endDayMinutes = date.getMinutes();"
                + "var endDaySeconds = date.getSeconds();"
                + "var endDayMs = date.getMilliseconds();");
        assertEquals(25, get("newDay"));
        assertEquals(3, get("newMonth"));
        assertEquals(23, get("endDayHours"));
        assertEquals(59, get("endDayMinutes"));
        assertEquals(59, get("endDaySeconds"));
        assertEquals(999, get("endDayMs"));
    }

    @Test
    void testDateComparison() {
        eval("var date1 = new Date(2025, 0, 1); var date2 = new Date(2025, 0, 2); var isDate2Greater = date2 > date1");
        assertTrue((Boolean) get("isDate2Greater"));
        // Test same day, different time
        eval("var dateEarly = new Date(2025, 0, 1, 9, 0, 0); var dateLate = new Date(2025, 0, 1, 17, 0, 0); var isDateLateGreater = dateLate > dateEarly;");
        assertTrue((Boolean) get("isDateLateGreater"));
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
        eval("var originalDate = new Date(2020, 0, 1);"
                + "var newTimestamp = new Date(2022, 0, 1).getTime();"
                + "originalDate.setTime(newTimestamp);"
                + "var afterSetYear = originalDate.getFullYear();");
        assertEquals(2022, get("afterSetYear"));

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
