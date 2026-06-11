package com.jsoncrud.model;

import com.jsonparser.model.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class RecordTest {

    public static int passed = 0;
    public static int failed = 0;

    public static void main(String[] args) {
        passed = 0; failed = 0;
        testGetId();
        testGetFields();
        testGetFieldsImmutable();
        testToJsonObject();
        testFromJsonObject();
        testRoundTrip();
        testToString();
        System.out.println("\n[RecordTest 결과] " + passed + " 통과 / " + failed + " 실패");
    }

    static void testGetId() {
        section("getId");
        assert_(record(1, "name", "Alice").getId() == 1,  "id=1");
        assert_(record(99, "k", "v").getId() == 99,       "id=99");
    }

    static void testGetFields() {
        section("getFields");
        Map<String, String> fields = map("name", "Alice", "city", "Seoul");
        Record r = new Record(1, fields);
        assert_("Alice".equals(r.getFields().get("name")), "name 필드");
        assert_("Seoul".equals(r.getFields().get("city")), "city 필드");
        assert_(r.getFields().size() == 2,                 "fields 크기");
    }

    static void testGetFieldsImmutable() {
        section("getFields 불변성");
        Record r = record(1, "k", "v");
        assertThrows(() -> r.getFields().put("x", "y"), UnsupportedOperationException.class, "put 차단");
    }

    static void testToJsonObject() {
        section("toJsonObject");
        Record r = record(7, "name", "Bob");
        JsonObject obj = r.toJsonObject();
        assert_(obj.get("id").asInt() == 7,              "id 직렬화");
        assert_(obj.get("fields").isObject(),            "fields 직렬화 타입");
        assert_("Bob".equals(obj.get("fields").asObject().get("name").asString()), "fields.name");

        // 빈 fields
        Record empty = new Record(2, new LinkedHashMap<>());
        JsonObject emptyObj = empty.toJsonObject();
        assert_(emptyObj.get("fields").asObject().size() == 0, "빈 fields 직렬화");
    }

    static void testFromJsonObject() {
        section("fromJsonObject");
        Record r = record(3, "email", "a@b.com");
        JsonObject obj = r.toJsonObject();
        Record restored = Record.fromJsonObject(obj);
        assert_(restored.getId() == 3,                         "id 역직렬화");
        assert_("a@b.com".equals(restored.getFields().get("email")), "email 역직렬화");

        // fields 키 없는 JsonObject
        JsonObject noFields = new JsonObject();
        noFields.put("id", new com.jsonparser.model.JsonNumber("5"));
        Record fromNoFields = Record.fromJsonObject(noFields);
        assert_(fromNoFields.getId() == 5,              "id without fields");
        assert_(fromNoFields.getFields().isEmpty(),     "fields 없으면 빈 Map");
    }

    static void testRoundTrip() {
        section("직렬화 왕복");
        Map<String, String> fields = map("a", "1", "b", "2", "c", "3");
        Record original = new Record(10, fields);
        Record restored = Record.fromJsonObject(original.toJsonObject());
        assert_(original.getId() == restored.getId(),                 "id 일치");
        assert_(original.getFields().equals(restored.getFields()),    "fields 일치");
    }

    static void testToString() {
        section("toString");
        String s = record(1, "name", "Alice").toString();
        assert_(s.contains("id=1"),    "toString id");
        assert_(s.contains("Alice"),   "toString fields 값");
    }

    // ── 헬퍼 ────────────────────────────────────────────────

    static Record record(int id, String k, String v) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(k, v);
        return new Record(id, m);
    }

    static Map<String, String> map(String... kvs) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kvs.length; i += 2) m.put(kvs[i], kvs[i + 1]);
        return m;
    }

    static void section(String name) { System.out.println("\n  [" + name + "]"); }

    static void assert_(boolean cond, String label) {
        if (cond) { System.out.println("    PASS: " + label); passed++; }
        else       { System.out.println("    FAIL: " + label); failed++; }
    }

    static void assertThrows(Runnable r, Class<? extends Throwable> type, String label) {
        try {
            r.run();
            System.out.println("    FAIL (예외 미발생): " + label);
            failed++;
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                System.out.println("    PASS (예외 발생): " + label);
                passed++;
            } else {
                System.out.println("    FAIL (예외 타입 불일치 " + t.getClass().getSimpleName() + "): " + label);
                failed++;
            }
        }
    }
}
