package com.jsonparser.model;

public class JsonNumber extends JsonValue {

    private final String raw;
    private final Number value;

    public JsonNumber(String raw) {
        this.raw = raw;
        this.value = parse(raw);
    }

    private Number parse(String s) {
        if (s.contains(".") || s.contains("e") || s.contains("E")) {
            return Double.parseDouble(s);
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return Double.parseDouble(s);
        }
    }

    public Number getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.NUMBER;
    }

    @Override
    public String toString() {
        return raw;
    }
}
