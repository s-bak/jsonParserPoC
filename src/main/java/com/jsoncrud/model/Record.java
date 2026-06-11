package com.jsoncrud.model;

import com.jsonparser.model.JsonArray;
import com.jsonparser.model.JsonObject;
import com.jsonparser.model.JsonString;
import com.jsonparser.model.JsonNumber;
import com.jsonparser.model.JsonValue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Record {

    private final int id;
    private final Map<String, String> fields;

    public Record(int id, Map<String, String> fields) {
        this.id     = id;
        this.fields = new LinkedHashMap<>(fields);
    }

    public int getId() {
        return id;
    }

    public Map<String, String> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.put("id", new JsonNumber(String.valueOf(id)));

        JsonObject fieldsObj = new JsonObject();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            fieldsObj.put(entry.getKey(), new JsonString(entry.getValue()));
        }
        obj.put("fields", fieldsObj);
        return obj;
    }

    public static Record fromJsonObject(JsonObject obj) {
        int id = obj.get("id").asInt();

        Map<String, String> fields = new LinkedHashMap<>();
        if (obj.has("fields") && obj.get("fields").isObject()) {
            JsonObject fieldsObj = obj.get("fields").asObject();
            for (Map.Entry<String, JsonValue> entry : fieldsObj.entries().entrySet()) {
                fields.put(entry.getKey(), entry.getValue().asString());
            }
        }
        return new Record(id, fields);
    }

    @Override
    public String toString() {
        return "Record{id=" + id + ", fields=" + fields + "}";
    }
}
