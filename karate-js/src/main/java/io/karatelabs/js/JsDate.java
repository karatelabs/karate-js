/*
 * The MIT License
 *
 * Copyright 2025 Karate Labs Inc.
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

public class JsDate extends JsObject implements Invokable {

    final ZonedDateTime dateTime;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final ZoneId UTC = ZoneId.of("UTC");

    public JsDate() {
        this(ZonedDateTime.now());
    }

    public JsDate(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public JsDate(long timestamp) {
        this.dateTime = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault());
    }

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

    public long getTime() {
        return dateTime.toInstant().toEpochMilli();
    }

    @Override
    public String toString() {
        return dateTime.toString();
    }

    @Override
    Prototype initPrototype() {
        Prototype wrapped = super.initPrototype();
        return new Prototype(wrapped) {
            @Override
            public Object getProperty(String propName) {
                switch (propName) {
                    case "now":
                        return (Invokable) args -> System.currentTimeMillis();
                    case "parse":
                        return (Invokable) args -> {
                            if (args.length == 0 || args[0] == null) {
                                return Undefined.NAN;
                            }
                            try {
                                String dateStr = args[0].toString();
                                return new JsDate(dateStr).getTime();
                            } catch (Exception e) {
                                return Undefined.NAN;
                            }
                        };
                    case "getTime":
                    case "valueOf":
                        return (Invokable) args -> {
                            if (thisObject instanceof JsDate) {
                                return ((JsDate) thisObject).getTime();
                            }
                            return getTime();
                        };
                    case "toString":
                        return (Invokable) args -> {
                            if (thisObject instanceof JsDate) {
                                return (thisObject).toString();
                            }
                            return toString();
                        };
                    case "toISOString":
                        return (Invokable) args -> {
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            return dt.withZoneSameInstant(UTC).format(ISO_FORMATTER);
                        };
                    case "toUTCString":
                        return (Invokable) args -> {
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            // Format: "Fri, 01 Jan 2021 00:00:00 GMT"
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
                            return dt.withZoneSameInstant(UTC).format(formatter);
                        };
                    case "getFullYear":
                        return (Invokable) args -> {
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            return dt.getYear();
                        };
                    case "getMonth":
                        return (Invokable) args -> {
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            // JavaScript months are 0-indexed
                            return dt.getMonthValue() - 1;
                        };
                    case "getDate":
                        return (Invokable) args -> {
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            return dt.getDayOfMonth();
                        };
                    case "getDay":
                        return (Invokable) args -> {
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            // Convert Java's 1-7 (Mon-Sun) to JavaScript's 0-6 (Sun-Sat)
                            return dt.getDayOfWeek().getValue() % 7;
                        };
                    case "getHours":
                        return (Invokable) args -> {
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            return dt.getHour();
                        };
                    case "getMinutes":
                        return (Invokable) args -> {
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            return dt.getMinute();
                        };
                    case "getSeconds":
                        return (Invokable) args -> {
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            return dt.getSecond();
                        };
                    case "getMilliseconds":
                        return (Invokable) args -> {
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            return dt.get(ChronoField.MILLI_OF_SECOND);
                        };

                }
                return null;
            }
        };
    }

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

}
