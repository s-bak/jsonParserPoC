package com.jsonparser.model;

public class JsonNull extends JsonValue {

    public static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {}

    @Override
    public Type getType() {
        return Type.NULL;
    }

    @Override
    public String toString() {
        return "null";
    }
}
