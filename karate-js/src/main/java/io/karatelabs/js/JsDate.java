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

public class JsDate extends JsObject {

    private ZonedDateTime dateTime;
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

    public JsDate(int year, int month, int date) {
        // JavaScript months are 0-indexed, Java months are 1-indexed
        this.dateTime = ZonedDateTime.of(year, month + 1, date, 0, 0, 0, 0, ZoneId.systemDefault());
    }

    public JsDate(int year, int month, int date, int hours, int minutes, int seconds) {
        this.dateTime = ZonedDateTime.of(year, month + 1, date, hours, minutes, seconds, 0, ZoneId.systemDefault());
    }

    public JsDate(int year, int month, int date, int hours, int minutes, int seconds, int ms) {
        this.dateTime = ZonedDateTime.of(year, month + 1, date, hours, minutes, seconds, ms * 1000000, ZoneId.systemDefault());
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

    public void setDateTime(ZonedDateTime newDateTime) {
        this.dateTime = newDateTime;
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
                    // Setters
                    case "setDate":
                        return (Invokable) args -> {
                            if (args.length == 0 || !(args[0] instanceof Number)) {
                                return Undefined.NAN;
                            }
                            int day = ((Number) args[0]).intValue();
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            ZonedDateTime newDt = dt.withDayOfMonth(day);
                            if (thisObject instanceof JsDate) {
                                ((JsDate) thisObject).setDateTime(newDt);
                            }
                            return newDt.toInstant().toEpochMilli();
                        };
                    case "setMonth":
                        return (Invokable) args -> {
                            if (args.length == 0 || !(args[0] instanceof Number)) {
                                return Undefined.NAN;
                            }
                            // JavaScript months are 0-indexed
                            int month = ((Number) args[0]).intValue() + 1;
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            ZonedDateTime newDt = dt.withMonth(month);
                            if (thisObject instanceof JsDate) {
                                ((JsDate) thisObject).setDateTime(newDt);
                            }
                            return newDt.toInstant().toEpochMilli();
                        };
                    case "setFullYear":
                        return (Invokable) args -> {
                            if (args.length == 0 || !(args[0] instanceof Number)) {
                                return Undefined.NAN;
                            }
                            int year = ((Number) args[0]).intValue();
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            ZonedDateTime newDt = dt.withYear(year);
                            if (thisObject instanceof JsDate) {
                                ((JsDate) thisObject).setDateTime(newDt);
                            }
                            return newDt.toInstant().toEpochMilli();
                        };
                    case "setHours":
                        return (Invokable) args -> {
                            if (args.length == 0 || !(args[0] instanceof Number)) {
                                return Undefined.NAN;
                            }
                            int hours = ((Number) args[0]).intValue();
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            ZonedDateTime newDt = dt.withHour(hours);
                            // Handle optional minute, second, and millisecond parameters
                            if (args.length > 1 && args[1] instanceof Number) {
                                newDt = newDt.withMinute(((Number) args[1]).intValue());
                            }
                            if (args.length > 2 && args[2] instanceof Number) {
                                newDt = newDt.withSecond(((Number) args[2]).intValue());
                            }
                            if (args.length > 3 && args[3] instanceof Number) {
                                newDt = newDt.with(ChronoField.MILLI_OF_SECOND, ((Number) args[3]).intValue());
                            }
                            if (thisObject instanceof JsDate) {
                                ((JsDate) thisObject).setDateTime(newDt);
                            }
                            return newDt.toInstant().toEpochMilli();
                        };
                    case "setMinutes":
                        return (Invokable) args -> {
                            if (args.length == 0 || !(args[0] instanceof Number)) {
                                return Undefined.NAN;
                            }
                            int minutes = ((Number) args[0]).intValue();
                            ZonedDateTime dt = thisObject instanceof JsDate
                                    ? ((JsDate) thisObject).dateTime : dateTime;
                            ZonedDateTime newDt = dt.withMinute(minutes);
                            // Handle optional second and millisecond parameters
                            if (args.length > 1 && args[1] instanceof Number) {
                                newDt = newDt.withSecond(((Number) args[1]).intValue());
                            }
                            if (args.length > 2 && args[2] instanceof Number) {
                                newDt = newDt.with(ChronoField.MILLI_OF_SECOND, ((Number) args[2]).intValue());
                            }
                            if (thisObject instanceof JsDate) {
                                ((JsDate) thisObject).setDateTime(newDt);
                            }
                            return newDt.toInstant().toEpochMilli();
                        };
                    case "setSeconds":
                        return (Invokable) args -> {
                            if (args.length == 0 || !(args[0] instanceof Number)) {
                                return Undefined.NAN;
                            }
                            int seconds = ((Number) args[0]).intValue();
                            ZonedDateTime dt = thisObject instanceof JsDate ? ((JsDate) thisObject).dateTime : dateTime;
                            ZonedDateTime newDt = dt.withSecond(seconds);
                            // Handle optional millisecond parameter
                            if (args.length > 1 && args[1] instanceof Number) {
                                newDt = newDt.with(ChronoField.MILLI_OF_SECOND, ((Number) args[1]).intValue());
                            }
                            if (thisObject instanceof JsDate) {
                                ((JsDate) thisObject).setDateTime(newDt);
                            }
                            return newDt.toInstant().toEpochMilli();
                        };
                    case "setMilliseconds":
                        return (Invokable) args -> {
                            if (args.length == 0 || !(args[0] instanceof Number)) {
                                return Undefined.NAN;
                            }
                            int ms = ((Number) args[0]).intValue();
                            ZonedDateTime dt = thisObject instanceof JsDate ? ((JsDate) thisObject).dateTime : dateTime;
                            ZonedDateTime newDt = dt.with(ChronoField.MILLI_OF_SECOND, ms);
                            if (thisObject instanceof JsDate) {
                                ((JsDate) thisObject).setDateTime(newDt);
                            }
                            return newDt.toInstant().toEpochMilli();
                        };
                    case "setTime":
                        return (Invokable) args -> {
                            if (args.length == 0 || !(args[0] instanceof Number)) {
                                return Undefined.NAN;
                            }
                            long timestamp = ((Number) args[0]).longValue();
                            ZonedDateTime newDt = ZonedDateTime.ofInstant(
                                    Instant.ofEpochMilli(timestamp),
                                    ZoneId.systemDefault());
                            if (thisObject instanceof JsDate) {
                                ((JsDate) thisObject).setDateTime(newDt);
                            }
                            return timestamp;
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
        } else if (args.length == 1) {
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
        } else if (args.length >= 3) {
            // Date(year, month, day, [hours, minutes, seconds, ms])
            int year = args[0] instanceof Number ? ((Number) args[0]).intValue() : 0;
            int month = args[1] instanceof Number ? ((Number) args[1]).intValue() : 0;
            int day = args[2] instanceof Number ? ((Number) args[2]).intValue() : 1;
            if (args.length >= 6) {
                int hours = args[3] instanceof Number ? ((Number) args[3]).intValue() : 0;
                int minutes = args[4] instanceof Number ? ((Number) args[4]).intValue() : 0;
                int seconds = args[5] instanceof Number ? ((Number) args[5]).intValue() : 0;

                if (args.length >= 7) {
                    int ms = args[6] instanceof Number ? ((Number) args[6]).intValue() : 0;
                    return new JsDate(year, month, day, hours, minutes, seconds, ms);
                }
                return new JsDate(year, month, day, hours, minutes, seconds);
            }
            return new JsDate(year, month, day);
        }
        return new JsDate();
    }

}
