package io.karatelabs.js;

public class ParserException extends RuntimeException {

    ParserException(String message) {
        super(message);
    }

    ParserException(String message, Throwable cause) {
        super(message, cause);
    }

}
