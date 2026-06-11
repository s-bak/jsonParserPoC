package com.jsonparser;

public class JsonToken {

    public enum Type {
        LEFT_BRACE,    // {
        RIGHT_BRACE,   // }
        LEFT_BRACKET,  // [
        RIGHT_BRACKET, // ]
        COLON,         // :
        COMMA,         // ,
        STRING,
        NUMBER,
        TRUE,
        FALSE,
        NULL,
        EOF
    }

    private final Type type;
    private final String value;
    private final int position;

    public JsonToken(Type type, String value, int position) {
        this.type     = type;
        this.value    = value;
        this.position = position;
    }

    public Type getType()     { return type; }
    public String getValue()  { return value; }
    public int getPosition()  { return position; }

    @Override
    public String toString() {
        return "Token(" + type + ", " + value + ", pos=" + position + ")";
    }
}
