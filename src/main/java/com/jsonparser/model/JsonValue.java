package com.jsonparser.model;

public abstract class JsonValue {

    public enum Type {
        OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL
    }

    public abstract Type getType();

    public boolean isObject()  { return getType() == Type.OBJECT; }
    public boolean isArray()   { return getType() == Type.ARRAY; }
    public boolean isString()  { return getType() == Type.STRING; }
    public boolean isNumber()  { return getType() == Type.NUMBER; }
    public boolean isBoolean() { return getType() == Type.BOOLEAN; }
    public boolean isNull()    { return getType() == Type.NULL; }

    public JsonObject asObject() {
        if (!isObject()) throw new ClassCastException("Not a JSON object, actual type: " + getType());
        return (JsonObject) this;
    }

    public JsonArray asArray() {
        if (!isArray()) throw new ClassCastException("Not a JSON array, actual type: " + getType());
        return (JsonArray) this;
    }

    public String asString() {
        if (!isString()) throw new ClassCastException("Not a JSON string, actual type: " + getType());
        return ((JsonString) this).getValue();
    }

    public Number asNumber() {
        if (!isNumber()) throw new ClassCastException("Not a JSON number, actual type: " + getType());
        return ((JsonNumber) this).getValue();
    }

    public double asDouble() {
        return asNumber().doubleValue();
    }

    public long asLong() {
        return asNumber().longValue();
    }

    public int asInt() {
        return asNumber().intValue();
    }

    public boolean asBoolean() {
        if (!isBoolean()) throw new ClassCastException("Not a JSON boolean, actual type: " + getType());
        return ((JsonBoolean) this).getValue();
    }

    @Override
    public abstract String toString();
}
