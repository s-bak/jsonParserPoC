package com.jsonparser;

import com.jsonparser.exception.JsonParseException;
import com.jsonparser.model.*;
import java.util.List;

public class JsonParserTest {

    public static int passed = 0;
    public static int failed = 0;

    public static void main(String[] args) {
        passed = 0; failed = 0;
        testPrimitives();
        testObject();
        testArray();
        testNested();
        testEdgeCases();
        testErrorCases();
        testPrettyPrint();

        System.out.println("\n===========================");
        System.out.println("결과: " + passed + " 통과 / " + failed + " 실패");
        System.out.println("===========================");
    }

    // ---------- 원시 타입 ----------

    static void testPrimitives() {
        section("원시 타입 파싱");

        JsonValue s = JsonParser.parse("\"hello world\"");
        assert_(s.isString() && s.asString().equals("hello world"), "문자열 파싱");

        JsonValue n1 = JsonParser.parse("42");
        assert_(n1.isNumber() && n1.asInt() == 42, "정수 파싱");

        JsonValue n2 = JsonParser.parse("3.14");
        assert_(n2.isNumber() && Math.abs(n2.asDouble() - 3.14) < 1e-9, "실수 파싱");

        JsonValue n3 = JsonParser.parse("-1.5e2");
        assert_(n3.isNumber() && n3.asDouble() == -150.0, "지수 표기 파싱");

        JsonValue t = JsonParser.parse("true");
        assert_(t.isBoolean() && t.asBoolean(), "true 파싱");

        JsonValue f = JsonParser.parse("false");
        assert_(f.isBoolean() && !f.asBoolean(), "false 파싱");

        JsonValue nil = JsonParser.parse("null");
        assert_(nil.isNull(), "null 파싱");

        JsonValue escaped = JsonParser.parse("\"line1\\nline2\\ttabbed\"");
        assert_(escaped.asString().contains("\n") && escaped.asString().contains("\t"), "이스케이프 문자 파싱");

        JsonValue unicode = JsonParser.parse("\"\\u0041\\u0042\\u0043\"");
        assert_(unicode.asString().equals("ABC"), "유니코드 이스케이프 파싱");
    }

    // ---------- 객체 ----------

    static void testObject() {
        section("JSON Object 파싱");

        String json = "{\"name\":\"Alice\",\"age\":30,\"active\":true}";
        JsonObject obj = JsonParser.parseObject(json);
        assert_(obj.size() == 3, "필드 개수");
        assert_("Alice".equals(obj.get("name").asString()), "name 필드");
        assert_(obj.get("age").asInt() == 30, "age 필드");
        assert_(obj.get("active").asBoolean(), "active 필드");
        assert_(!obj.has("missing"), "없는 필드 has()");

        String empty = "{}";
        assert_(JsonParser.parseObject(empty).size() == 0, "빈 오브젝트");
    }

    // ---------- 배열 ----------

    static void testArray() {
        section("JSON Array 파싱");

        String json = "[1, \"two\", true, null, 3.5]";
        JsonArray arr = JsonParser.parseArray(json);
        assert_(arr.size() == 5, "배열 크기");
        assert_(arr.get(0).asInt() == 1, "인덱스 0");
        assert_("two".equals(arr.get(1).asString()), "인덱스 1");
        assert_(arr.get(2).asBoolean(), "인덱스 2");
        assert_(arr.get(3).isNull(), "인덱스 3 null");
        assert_(arr.get(4).asDouble() == 3.5, "인덱스 4");

        assert_(JsonParser.parseArray("[]").size() == 0, "빈 배열");
    }

    // ---------- 중첩 구조 ----------

    static void testNested() {
        section("중첩 구조 파싱");

        String json = "{"
            + "\"user\":{"
            +   "\"id\":1,"
            +   "\"name\":\"Bob\","
            +   "\"tags\":[\"admin\",\"user\"],"
            +   "\"address\":{\"city\":\"Seoul\",\"zip\":\"04524\"}"
            + "},"
            + "\"scores\":[10,20,30]"
            + "}";

        JsonObject root = JsonParser.parseObject(json);
        JsonObject user = root.get("user").asObject();
        assert_("Bob".equals(user.get("name").asString()), "중첩 객체 name");
        assert_(user.get("id").asInt() == 1, "중첩 객체 id");

        JsonArray tags = user.get("tags").asArray();
        assert_(tags.size() == 2, "태그 배열 크기");
        assert_("admin".equals(tags.get(0).asString()), "태그[0]");

        JsonObject addr = user.get("address").asObject();
        assert_("Seoul".equals(addr.get("city").asString()), "중첩 주소 city");

        List<JsonValue> scores = root.get("scores").asArray().elements();
        int sum = scores.stream().mapToInt(JsonValue::asInt).sum();
        assert_(sum == 60, "scores 합산");
    }

    // ---------- 엣지 케이스 ----------

    static void testEdgeCases() {
        section("엣지 케이스");

        JsonValue ws = JsonParser.parse("  {  \"k\"  :  \"v\"  }  ");
        assert_(ws.isObject(), "공백 허용");

        JsonValue longNum = JsonParser.parse("9999999999999");
        assert_(longNum.asLong() == 9999999999999L, "Long 범위 정수");

        JsonValue negZero = JsonParser.parse("-0");
        assert_(negZero.isNumber(), "-0 파싱");

        String unicodeKey = "{\"\\u0041BC\":1}";
        JsonObject uObj = JsonParser.parseObject(unicodeKey);
        assert_(uObj.has("ABC"), "유니코드 키");

        String deepJson = "{\"a\":{\"b\":{\"c\":{\"d\":42}}}}";
        int val = JsonParser.parseObject(deepJson)
            .get("a").asObject()
            .get("b").asObject()
            .get("c").asObject()
            .get("d").asInt();
        assert_(val == 42, "4단계 중첩");
    }

    // ---------- 오류 케이스 ----------

    static void testErrorCases() {
        section("오류 처리");

        assertThrows("{\"key\": }",        "값 없이 닫힘");
        assertThrows("{key: 1}",           "키에 따옴표 없음");
        assertThrows("[1, 2,]",            "후행 쉼표 (배열)");
        assertThrows("{\"a\":1,}",         "후행 쉼표 (객체)");
        assertThrows("\"unterminated",     "닫히지 않은 문자열");
        assertThrows("{",                  "닫히지 않은 객체");
        assertThrows("tru",               "잘못된 리터럴");
        assertThrows("",                   "빈 입력 (blank)");
        assertThrows(null,                 "null 입력");
        assertThrows("{} extra",           "루트 이후 여분 토큰");
        assertThrowsAny(() -> JsonParser.parseObject("[1,2,3]"),   "parseObject에 배열 입력");
        assertThrowsAny(() -> JsonParser.parseArray("{\"k\":1}"),  "parseArray에 객체 입력");
    }

    // ---------- Pretty Print ----------

    static void testPrettyPrint() {
        section("Pretty Print");

        String json = "{\"name\":\"Alice\",\"scores\":[1,2,3]}";
        JsonObject obj = JsonParser.parseObject(json);
        String pretty = obj.toPrettyString(0);
        assert_(pretty.contains("\n"), "줄바꿈 포함");
        assert_(pretty.contains("  "), "들여쓰기 포함");
        System.out.println(pretty);
    }

    // ---------- 헬퍼 ----------

    static void section(String name) {
        System.out.println("\n[" + name + "]");
    }

    static void assert_(boolean condition, String label) {
        if (condition) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label);
            failed++;
        }
    }

    static void assertThrows(String json, String label) {
        try {
            JsonParser.parse(json);
            System.out.println("  FAIL (예외 미발생): " + label);
            failed++;
        } catch (JsonParseException | IllegalArgumentException e) {
            System.out.println("  PASS (예외 발생): " + label + " -> " + e.getMessage());
            passed++;
        }
    }

    static void assertThrowsAny(Runnable r, String label) {
        try {
            r.run();
            System.out.println("  FAIL (예외 미발생): " + label);
            failed++;
        } catch (Exception e) {
            System.out.println("  PASS (예외 발생): " + label + " -> " + e.getMessage());
            passed++;
        }
    }
}
