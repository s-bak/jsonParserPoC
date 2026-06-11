package com.jsoncrud.repository;

import com.jsonparser.JsonParser;
import com.jsonparser.model.JsonArray;
import com.jsonparser.model.JsonObject;
import com.jsoncrud.model.Record;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonFileRepository {

    private static final String EMPTY_JSON = "{\n  \"records\": []\n}";

    private final Path filePath;

    public JsonFileRepository(String filePath) {
        this.filePath = Paths.get(filePath);
        init();
    }

    private void init() {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            if (!Files.exists(filePath)) {
                Files.writeString(filePath, EMPTY_JSON, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("데이터 파일 초기화 실패: " + filePath, e);
        }
    }

    public List<Record> findAll() {
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return new ArrayList<>();
            }
            JsonObject root = JsonParser.parse(content).asObject();
            if (!root.has("records") || !root.get("records").isArray()) {
                return new ArrayList<>();
            }
            JsonArray arr = root.get("records").asArray();
            List<Record> records = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                records.add(Record.fromJsonObject(arr.get(i).asObject()));
            }
            return records;
        } catch (IOException e) {
            throw new RuntimeException("데이터 파일 읽기 실패: " + filePath, e);
        }
    }

    public void saveAll(List<Record> records) {
        JsonArray arr = new JsonArray();
        for (Record r : records) {
            arr.add(r.toJsonObject());
        }
        JsonObject root = new JsonObject();
        root.put("records", arr);

        try {
            Files.writeString(filePath, root.toPrettyString(0), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("데이터 파일 쓰기 실패: " + filePath, e);
        }
    }
}
