/*
 * The MIT License
 *
 * Copyright 2024 Karate Labs Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.karatelabs.js;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Map;

/**
 * A JavaScript Date implementation using Java's modern Time API.
 * This class uses ZonedDateTime internally for better time zone support.
 */
public class JsDate extends JsObject {

    final ZonedDateTime dateTime;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final ZoneId UTC = ZoneId.of("UTC");

    /**
     * Create a new JsDate with the current time.
     */
    public JsDate() {
        this(ZonedDateTime.now());
    }

    /**
     * Create a new JsDate with the specified ZonedDateTime.
     * 
     * @param dateTime The ZonedDateTime to use
     */
    public JsDate(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Create a new JsDate with the specified timestamp.
     * 
     * @param timestamp The timestamp in milliseconds since epoch
     */
    public JsDate(long timestamp) {
        this.dateTime = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault());
    }

    /**
     * Create a new JsDate from a string representation.
     * 
     * @param dateStr The date string to parse
     */
    public JsDate(String dateStr) {
        ZonedDateTime parsedDateTime;
        try {
            // Try parsing as ISO format
            LocalDateTime localDateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
            parsedDateTime = ZonedDateTime.of(localDateTime, UTC);
        } catch (DateTimeParseException e) {
            try {
                // Try parsing as ISO date only
                LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
                parsedDateTime = localDate.atStartOfDay(UTC);
            } catch (DateTimeParseException e2) {
                try {
                    // Try parsing with offset
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateStr);
                    parsedDateTime = offsetDateTime.toZonedDateTime();
                } catch (DateTimeParseException e3) {
                    // If all parsing fails, use current time
                    parsedDateTime = ZonedDateTime.now();
                }
            }
        }
        this.dateTime = parsedDateTime;
    }

    /**
     * Get the timestamp representation of this date.
     * 
     * @return The timestamp in milliseconds since epoch
     */
    public long getTime() {
        return dateTime.toInstant().toEpochMilli();
    }

    @Override
    public String toString() {
        return dateTime.toString();
    }

    @Override
    Map<String, Object> initPrototype() {
        Map<String, Object> prototype = super.initPrototype();
        
        // Constructor
        prototype.put("constructor", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                if (args.length == 0) {
                    return new JsDate();
                } else {
                    Object arg = args[0];
                    if (arg instanceof Number) {
                        // Date(timestamp)
                        return new JsDate(((Number) arg).longValue());
                    } else if (arg instanceof String) {
                        // Date(dateString)
                        return new JsDate((String) arg);
                    } else if (arg instanceof JsDate) {
                        // Date(dateObject)
                        return new JsDate(((JsDate) arg).dateTime);
                    }
                }
                return new JsDate();
            }
        });
        
        // Static methods
        prototype.put("now", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                return System.currentTimeMillis();
            }
        });
        
        prototype.put("parse", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                if (args.length == 0 || args[0] == null) {
                    return Undefined.NAN;
                }
                try {
                    String dateStr = args[0].toString();
                    return new JsDate(dateStr).getTime();
                } catch (Exception e) {
                    return Undefined.NAN;
                }
            }
        });
        
        // Instance methods
        prototype.put("getTime", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                if (thisObject instanceof JsDate) {
                    return ((JsDate) thisObject).getTime();
                }
                return getTime();
            }
        });
        
        prototype.put("toString", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                if (thisObject instanceof JsDate) {
                    return ((JsDate) thisObject).toString();
                }
                return toString();
            }
        });
        
        prototype.put("toISOString", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ZonedDateTime dt = thisObject instanceof JsDate 
                        ? ((JsDate) thisObject).dateTime : dateTime;
                return dt.withZoneSameInstant(UTC).format(ISO_FORMATTER);
            }
        });
        
        prototype.put("toUTCString", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ZonedDateTime dt = thisObject instanceof JsDate 
                        ? ((JsDate) thisObject).dateTime : dateTime;
                // Format: "Fri, 01 Jan 2021 00:00:00 GMT"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
                return dt.withZoneSameInstant(UTC).format(formatter);
            }
        });
        
        prototype.put("valueOf", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                if (thisObject instanceof JsDate) {
                    return ((JsDate) thisObject).getTime();
                }
                return getTime();
            }
        });
        
        // Get methods - using the local time zone
        prototype.put("getFullYear", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ZonedDateTime dt = thisObject instanceof JsDate 
                        ? ((JsDate) thisObject).dateTime : dateTime;
                return dt.getYear();
            }
        });
        
        prototype.put("getMonth", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ZonedDateTime dt = thisObject instanceof JsDate 
                        ? ((JsDate) thisObject).dateTime : dateTime;
                // JavaScript months are 0-indexed
                return dt.getMonthValue() - 1;
            }
        });
        
        prototype.put("getDate", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ZonedDateTime dt = thisObject instanceof JsDate 
                        ? ((JsDate) thisObject).dateTime : dateTime;
                return dt.getDayOfMonth();
            }
        });
        
        prototype.put("getDay", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ZonedDateTime dt = thisObject instanceof JsDate 
                        ? ((JsDate) thisObject).dateTime : dateTime;
                // Convert Java's 1-7 (Mon-Sun) to JavaScript's 0-6 (Sun-Sat)
                int day = dt.getDayOfWeek().getValue() % 7;
                return day;
            }
        });
        
        prototype.put("getHours", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ZonedDateTime dt = thisObject instanceof JsDate 
                        ? ((JsDate) thisObject).dateTime : dateTime;
                return dt.getHour();
            }
        });
        
        prototype.put("getMinutes", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ZonedDateTime dt = thisObject instanceof JsDate 
                        ? ((JsDate) thisObject).dateTime : dateTime;
                return dt.getMinute();
            }
        });
        
        prototype.put("getSeconds", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ZonedDateTime dt = thisObject instanceof JsDate 
                        ? ((JsDate) thisObject).dateTime : dateTime;
                return dt.getSecond();
            }
        });
        
        prototype.put("getMilliseconds", new JsFunction() {
            @Override
            public Object invoke(Object... args) {
                ZonedDateTime dt = thisObject instanceof JsDate 
                        ? ((JsDate) thisObject).dateTime : dateTime;
                return dt.get(ChronoField.MILLI_OF_SECOND);
            }
        });
        
        return prototype;
    }
}
