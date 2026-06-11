package com.jsonparser.model;

import java.util.*;

public class JsonObject extends JsonValue {

    private final Map<String, JsonValue> fields = new LinkedHashMap<>();

    public void put(String key, JsonValue value) {
        fields.put(key, value);
    }

    public JsonValue get(String key) {
        return fields.get(key);
    }

    public boolean has(String key) {
        return fields.containsKey(key);
    }

    public Set<String> keys() {
        return Collections.unmodifiableSet(fields.keySet());
    }

    public Map<String, JsonValue> entries() {
        return Collections.unmodifiableMap(fields);
    }

    public int size() {
        return fields.size();
    }

    @Override
    public Type getType() {
        return Type.OBJECT;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        Iterator<Map.Entry<String, JsonValue>> it = fields.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, JsonValue> entry = it.next();
            sb.append("\"").append(entry.getKey()).append("\":")
              .append(entry.getValue().toString());
            if (it.hasNext()) sb.append(",");
        }
        return sb.append("}").toString();
    }

    public String toPrettyString(int indent) {
        if (fields.isEmpty()) return "{}";
        String pad    = " ".repeat(indent + 2);
        String padEnd = " ".repeat(indent);
        StringBuilder sb = new StringBuilder("{\n");
        Iterator<Map.Entry<String, JsonValue>> it = fields.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, JsonValue> entry = it.next();
            sb.append(pad).append("\"").append(entry.getKey()).append("\": ");
            JsonValue v = entry.getValue();
            if (v instanceof JsonObject) sb.append(((JsonObject) v).toPrettyString(indent + 2));
            else if (v instanceof JsonArray) sb.append(((JsonArray) v).toPrettyString(indent + 2));
            else sb.append(v.toString());
            if (it.hasNext()) sb.append(",");
            sb.append("\n");
        }
        return sb.append(padEnd).append("}").toString();
    }
}
