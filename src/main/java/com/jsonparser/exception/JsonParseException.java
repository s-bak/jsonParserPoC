package com.jsonparser.exception;

public class JsonParseException extends RuntimeException {

    private final int position;

    public JsonParseException(String message, int position) {
        super(message + " (position: " + position + ")");
        this.position = position;
    }

    public JsonParseException(String message) {
        super(message);
        this.position = -1;
    }

    public int getPosition() {
        return position;
    }
}
