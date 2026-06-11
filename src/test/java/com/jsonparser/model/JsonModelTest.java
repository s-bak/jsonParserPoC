package com.jsonparser.model;

public class JsonModelTest {

    public static int passed = 0;
    public static int failed = 0;

    public static void main(String[] args) {
        passed = 0; failed = 0;
        testJsonNull();
        testJsonBoolean();
        testJsonString();
        testJsonNumber();
        testJsonObject();
        testJsonArray();
        testJsonValueCastExceptions();
        testJsonValueTypeChecks();
        System.out.println("\n[JsonModelTest 결과] " + passed + " 통과 / " + failed + " 실패");
    }

    // ── JsonNull ─────────────────────────────────────────────

    static void testJsonNull() {
        section("JsonNull");
        assert_(JsonNull.INSTANCE == JsonNull.INSTANCE,         "싱글턴 동일성");
        assert_(JsonNull.INSTANCE.getType() == JsonValue.Type.NULL, "getType NULL");
        assert_(JsonNull.INSTANCE.isNull(),                     "isNull true");
        assert_("null".equals(JsonNull.INSTANCE.toString()),    "toString");
    }

    // ── JsonBoolean ───────────────────────────────────────────

    static void testJsonBoolean() {
        section("JsonBoolean");
        assert_(JsonBoolean.TRUE.getValue(),               "TRUE.getValue()");
        assert_(!JsonBoolean.FALSE.getValue(),             "FALSE.getValue()");
        assert_(JsonBoolean.of(true)  == JsonBoolean.TRUE,  "of(true) → TRUE");
        assert_(JsonBoolean.of(false) == JsonBoolean.FALSE, "of(false) → FALSE");
        assert_(JsonBoolean.TRUE.getType()  == JsonValue.Type.BOOLEAN, "getType BOOLEAN");
        assert_(JsonBoolean.TRUE.isBoolean(),  "isBoolean true");
        assert_("true".equals(JsonBoolean.TRUE.toString()),   "toString true");
        assert_("false".equals(JsonBoolean.FALSE.toString()), "toString false");
    }

    // ── JsonString ────────────────────────────────────────────

    static void testJsonString() {
        section("JsonString");
        JsonString s = new JsonString("hello");
        assert_("hello".equals(s.getValue()),              "getValue");
        assert_(s.getType() == JsonValue.Type.STRING,      "getType STRING");
        assert_(s.isString(),                              "isString true");
        assert_("\"hello\"".equals(s.toString()),          "toString 일반");

        // 이스케이프 검증
        assert_(new JsonString("a\"b").toString().contains("\\\""), "이스케이프 \"");
        assert_(new JsonString("a\\b").toString().contains("\\\\"), "이스케이프 \\");
        assert_(new JsonString("a\nb").toString().contains("\\n"),  "이스케이프 \\n");
        assert_(new JsonString("a\rb").toString().contains("\\r"),  "이스케이프 \\r");
        assert_(new JsonString("a\tb").toString().contains("\\t"),  "이스케이프 \\t");
    }

    // ── JsonNumber ────────────────────────────────────────────

    static void testJsonNumber() {
        section("JsonNumber");
        JsonNumber intNum = new JsonNumber("42");
        assert_(intNum.asInt() == 42,                       "정수 asInt");
        assert_(intNum.asLong() == 42L,                     "정수 asLong");
        assert_(intNum.getType() == JsonValue.Type.NUMBER,  "getType NUMBER");
        assert_(intNum.isNumber(),                          "isNumber true");
        assert_("42".equals(intNum.toString()),             "toString 정수");

        JsonNumber negNum = new JsonNumber("-7");
        assert_(negNum.asInt() == -7,                       "음수 asInt");

        JsonNumber dblNum = new JsonNumber("3.14");
        assert_(Math.abs(dblNum.asDouble() - 3.14) < 1e-9, "실수 asDouble");

        JsonNumber expNum = new JsonNumber("1e2");
        assert_(expNum.asDouble() == 100.0,                 "지수 asDouble");

        JsonNumber longNum = new JsonNumber("9999999999999");
        assert_(longNum.asLong() == 9999999999999L,         "Long 범위");
    }

    // ── JsonObject ────────────────────────────────────────────

    static void testJsonObject() {
        section("JsonObject");
        JsonObject obj = new JsonObject();
        obj.put("name", new JsonString("Alice"));
        obj.put("age",  new JsonNumber("30"));

        assert_("Alice".equals(obj.get("name").asString()),     "put/get");
        assert_(obj.has("name"),                                "has 존재");
        assert_(!obj.has("missing"),                            "has 비존재");
        assert_(obj.size() == 2,                                "size");
        assert_(obj.keys().contains("name"),                    "keys 포함");
        assert_(obj.entries().size() == 2,                      "entries 크기");
        assertThrows(() -> obj.entries().clear(), UnsupportedOperationException.class, "entries 불변");

        // toString
        JsonObject single = new JsonObject();
        single.put("k", new JsonString("v"));
        assert_("{\"k\":\"v\"}".equals(single.toString()),     "toString 단일");

        JsonObject empty = new JsonObject();
        assert_("{}".equals(empty.toString()),                  "toString 빈");
        assert_("{}".equals(empty.toPrettyString(0)),          "toPrettyString 빈");

        // toPrettyString
        String pretty = single.toPrettyString(0);
        assert_(pretty.contains("\n"),  "toPrettyString 줄바꿈");
        assert_(pretty.contains("  "),  "toPrettyString 들여쓰기");

        // 중첩 Object → toPrettyString 재귀
        JsonObject outer = new JsonObject();
        outer.put("inner", single);
        String prettyNested = outer.toPrettyString(0);
        assert_(prettyNested.contains("\"k\""),  "중첩 Object toPrettyString");

        // 중첩 Array → toPrettyString 재귀
        JsonArray arr = new JsonArray();
        arr.add(new JsonNumber("1"));
        JsonObject withArr = new JsonObject();
        withArr.put("list", arr);
        String prettyArr = withArr.toPrettyString(0);
        assert_(prettyArr.contains("\"list\""), "중첩 Array toPrettyString");
    }

    // ── JsonArray ─────────────────────────────────────────────

    static void testJsonArray() {
        section("JsonArray");
        JsonArray arr = new JsonArray();
        arr.add(new JsonNumber("1"));
        arr.add(new JsonString("two"));

        assert_(arr.get(0).asInt() == 1,          "add/get 인덱스 0");
        assert_("two".equals(arr.get(1).asString()), "add/get 인덱스 1");
        assert_(arr.size() == 2,                   "size");
        assert_(arr.elements().size() == 2,        "elements 크기");
        assertThrows(() -> arr.elements().clear(), UnsupportedOperationException.class, "elements 불변");

        // toString
        JsonArray empty = new JsonArray();
        assert_("[]".equals(empty.toString()),              "toString 빈");
        assert_("[]".equals(empty.toPrettyString(0)),      "toPrettyString 빈");
        assert_("[1,\"two\"]".equals(arr.toString()),      "toString 복수");

        // toPrettyString
        String pretty = arr.toPrettyString(0);
        assert_(pretty.contains("\n"),  "toPrettyString 줄바꿈");
        assert_(pretty.contains("  "),  "toPrettyString 들여쓰기");

        // 중첩 Object → toPrettyString 재귀
        JsonObject obj = new JsonObject();
        obj.put("x", new JsonNumber("1"));
        JsonArray withObj = new JsonArray();
        withObj.add(obj);
        assert_(withObj.toPrettyString(0).contains("\"x\""), "중첩 Object toPrettyString");

        // 중첩 Array → toPrettyString 재귀
        JsonArray inner = new JsonArray();
        inner.add(new JsonNumber("9"));
        JsonArray withArr = new JsonArray();
        withArr.add(inner);
        assert_(withArr.toPrettyString(0).contains("9"), "중첩 Array toPrettyString");
    }

    // ── JsonValue 캐스트 예외 ─────────────────────────────────

    static void testJsonValueCastExceptions() {
        section("JsonValue 캐스트 예외");
        JsonValue str  = new JsonString("s");
        JsonValue num  = new JsonNumber("1");
        JsonValue bool = JsonBoolean.TRUE;
        JsonValue nil  = JsonNull.INSTANCE;
        JsonValue arr  = new JsonArray();
        JsonValue obj  = new JsonObject();

        assertThrows(str::asObject,  ClassCastException.class, "asObject on STRING");
        assertThrows(str::asArray,   ClassCastException.class, "asArray on STRING");
        assertThrows(num::asString,  ClassCastException.class, "asString on NUMBER");
        assertThrows(str::asNumber,  ClassCastException.class, "asNumber on STRING");
        assertThrows(str::asBoolean, ClassCastException.class, "asBoolean on STRING");
        assertThrows(str::asDouble,  ClassCastException.class, "asDouble on STRING");
        assertThrows(str::asLong,    ClassCastException.class, "asLong on STRING");
        assertThrows(str::asInt,     ClassCastException.class, "asInt on STRING");
    }

    // ── JsonValue 타입 체크 교차 검증 ─────────────────────────

    static void testJsonValueTypeChecks() {
        section("JsonValue 타입 체크");
        JsonValue[] values = {
            new JsonObject(), new JsonArray(), new JsonString("s"),
            new JsonNumber("1"), JsonBoolean.TRUE, JsonNull.INSTANCE
        };
        boolean[] expectations = {true, false, false, false, false, false};
        // isObject
        for (int i = 0; i < values.length; i++) {
            assert_(values[i].isObject() == expectations[i], "isObject[" + i + "]");
        }
        // isArray
        boolean[] arrExp = {false, true, false, false, false, false};
        for (int i = 0; i < values.length; i++) {
            assert_(values[i].isArray() == arrExp[i], "isArray[" + i + "]");
        }
        // isString, isNumber, isBoolean, isNull
        assert_(values[2].isString(),  "isString");
        assert_(values[3].isNumber(),  "isNumber");
        assert_(values[4].isBoolean(), "isBoolean");
        assert_(values[5].isNull(),    "isNull");
    }

    // ── 헬퍼 ────────────────────────────────────────────────

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
