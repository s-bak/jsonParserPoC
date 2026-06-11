package com.jsonparser.model;

import java.util.*;

public class JsonArray extends JsonValue {

    private final List<JsonValue> elements = new ArrayList<>();

    public void add(JsonValue value) {
        elements.add(value);
    }

    public JsonValue get(int index) {
        return elements.get(index);
    }

    public int size() {
        return elements.size();
    }

    public List<JsonValue> elements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public Type getType() {
        return Type.ARRAY;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(elements.get(i).toString());
        }
        return sb.append("]").toString();
    }

    public String toPrettyString(int indent) {
        if (elements.isEmpty()) return "[]";
        String pad    = " ".repeat(indent + 2);
        String padEnd = " ".repeat(indent);
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < elements.size(); i++) {
            sb.append(pad);
            JsonValue v = elements.get(i);
            if (v instanceof JsonObject) sb.append(((JsonObject) v).toPrettyString(indent + 2));
            else if (v instanceof JsonArray) sb.append(((JsonArray) v).toPrettyString(indent + 2));
            else sb.append(v.toString());
            if (i < elements.size() - 1) sb.append(",");
            sb.append("\n");
        }
        return sb.append(padEnd).append("]").toString();
    }
}
