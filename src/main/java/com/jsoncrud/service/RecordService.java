package com.jsoncrud.service;

import com.jsoncrud.model.Record;
import com.jsoncrud.repository.JsonFileRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RecordService {

    private final JsonFileRepository repository;

    public RecordService(JsonFileRepository repository) {
        this.repository = repository;
    }

    public Record create(Map<String, String> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("fields는 비어있을 수 없습니다.");
        }
        List<Record> records = repository.findAll();
        int nextId = records.stream()
                .mapToInt(Record::getId)
                .max()
                .orElse(0) + 1;
        Record newRecord = new Record(nextId, fields);
        records.add(newRecord);
        repository.saveAll(records);
        return newRecord;
    }

    public List<Record> findAll() {
        return repository.findAll();
    }

    public Optional<Record> findById(int id) {
        return repository.findAll().stream()
                .filter(r -> r.getId() == id)
                .findFirst();
    }

    public List<Record> findByField(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("검색 키는 비어있을 수 없습니다.");
        }
        String lowerValue = value == null ? "" : value.toLowerCase();
        List<Record> result = new ArrayList<>();
        for (Record r : repository.findAll()) {
            String fieldValue = r.getFields().get(key);
            if (fieldValue != null && fieldValue.toLowerCase().contains(lowerValue)) {
                result.add(r);
            }
        }
        return result;
    }

    public Record update(int id, String fieldKey, String newValue) {
        List<Record> records = repository.findAll();
        int index = indexById(records, id);

        Map<String, String> updatedFields = new LinkedHashMap<>(records.get(index).getFields());
        if (!updatedFields.containsKey(fieldKey)) {
            throw new IllegalArgumentException("필드 '" + fieldKey + "' 가 존재하지 않습니다.");
        }
        updatedFields.put(fieldKey, newValue);
        Record updated = new Record(id, updatedFields);
        records.set(index, updated);
        repository.saveAll(records);
        return updated;
    }

    public Record delete(int id) {
        List<Record> records = repository.findAll();
        int index = indexById(records, id);
        Record removed = records.remove(index);
        repository.saveAll(records);
        return removed;
    }

    private int indexById(List<Record> records, int id) {
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getId() == id) return i;
        }
        throw new IllegalArgumentException("ID " + id + " 를 찾을 수 없습니다.");
    }
}
